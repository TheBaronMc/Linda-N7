package linda.shm;

import linda.Callback;
import linda.Linda;
import linda.Tuple;

import java.io.*;
import java.util.*;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;

/** Shared memory implementation of Linda. */
public class CentralizedLinda implements Linda {

    public static final String SAVE_FILE_PATH = "linda_save";

    private ArrayList<Tuple> tuplesList;
    private ArrayList<EventHandler> readEvents;
    private ArrayList<EventHandler> takeEvents;

    private Lock mutex;
    private Condition readCondition;
    private Condition takeCondition;

    public CentralizedLinda() {
        this.tuplesList = new ArrayList<>();
        this.readEvents = new ArrayList<>();
        this.takeEvents = new ArrayList<>();

        this.mutex = new ReentrantLock();
        this.readCondition = this.mutex.newCondition();
        this.takeCondition = this.mutex.newCondition();

        this.load();
    }

    @Override
    public void write(Tuple t) {
        this.mutex.lock();

        this.debug("Write : Iteration sur les read callbacks");
        // Read Callbacks
        ArrayList<EventHandler> readEventsToCall = new ArrayList<>();
        Iterator<EventHandler> iterReadEvents = this.readEvents.iterator();
        while (iterReadEvents.hasNext()) {
            EventHandler event = iterReadEvents.next();
            if (event.worksWith(t)) {
                this.debug("Write : read - match avec un callback");
                readEventsToCall.add(event);
                this.debug("Write : read - suppresion de la liste des callbacks");
                iterReadEvents.remove();
            }
        }

        if (readEventsToCall.size() != 0)
            this.debug("Write : read - Appel des martching callbacks");
        for (EventHandler event : readEventsToCall) {
            this.mutex.unlock();
            event.callWith(t.deepclone());
            this.mutex.lock();
        }

        this.debug("Write: réveille des reads en attente");
        this.readCondition.signalAll();

        // Take Callbacks
        this.debug("Write : Iteration sur les take callbacks");
        EventHandler takeEventToCall = null;
        Boolean eventMatch = false;
        Iterator<EventHandler> iterTakeEvents = this.takeEvents.iterator();
        while (iterTakeEvents.hasNext() && !eventMatch) {
            EventHandler event = iterTakeEvents.next();
            if (event.worksWith(t)) {
                this.debug("Write : take - match avec un callback");
                takeEventToCall = event;
                iterTakeEvents.remove();
                eventMatch = true;
            }
        }

        if (takeEventToCall != null) {
            this.mutex.unlock();
            takeEventToCall.callWith(t);
            return;
        }

        this.debug("Ajout du tuple à la liste");
        this.tuplesList.add(t);

        this.debug("Reveille des take");
        this.takeCondition.signalAll();

        this.debug("libération du lock");
        this.mutex.unlock();
    }

    @Override
    public Tuple take(Tuple template) {
        this.mutex.lock();

        while (!existTuple(template)) {
            try {
                this.debug("Take : Aucun match");
                this.debug("Take : libération du lock");
                this.mutex.unlock();
                this.takeCondition.await();
                this.mutex.lock();
                this.debug("Take : récupération du lock");
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        this.debug("Take : match et récupération sur tuple");
        Tuple tuple = this.getTuple(template);

        this.debug("Take : libération du lock");
        this.mutex.unlock();

        return tuple;
    }

    @Override
    public Tuple read(Tuple template) {
        this.mutex.lock();

        while (!this.existTuple(template)) {
            try {
                this.debug("Read : Aucun match");
                this.debug("Read : Libération du lock");
                this.mutex.unlock();
                this.readCondition.await();
                this.mutex.lock();
                this.debug("Read : Récupération du lock");
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        this.debug("Read : Un tuple match");
        Tuple tuple = this.copyTuple(template);

        this.debug("Read : Libération du lock");
        this.mutex.unlock();

        return tuple;
    }

    @Override
    public Tuple tryTake(Tuple template) {
        this.mutex.lock();
        Tuple tuple = this.getTuple(template);
        this.mutex.unlock();

        return tuple;
    }

    @Override
    public Tuple tryRead(Tuple template) {
        this.mutex.lock();
        Tuple tuple = this.copyTuple(template);
        this.mutex.unlock();

        return tuple;
    }

    @Override
    public Collection<Tuple> takeAll(Tuple template) {
        ArrayList<Tuple> col = new ArrayList<>();

        this.mutex.lock();

        while (this.existTuple(template)) {
            col.add(this.getTuple(template));
        }

        this.mutex.unlock();

        return col;
    }

    @Override
    public Collection<Tuple> readAll(Tuple template) {
        ArrayList<Tuple> col = new ArrayList<>();

        this.mutex.lock();

        for (Tuple tuple : this.tuplesList) {
            if (tuple.matches(template)) {
                col.add(tuple.deepclone());
            }
        }

        this.mutex.unlock();

        return col;
    }

    @Override
    public void eventRegister(eventMode mode, eventTiming timing, Tuple template, Callback callback) {
        Tuple tuple = null;

        if (timing == eventTiming.IMMEDIATE) {
            switch (mode) {
                case READ -> tuple = this.tryRead(template);
                case TAKE -> tuple = this.tryTake(template);
                default -> throw new RuntimeException("Unknow mode : " + mode);
            }
        }

        if (tuple == null) {
            this.mutex.lock();

            switch (mode) {
                case READ -> this.readEvents.add(new EventHandler(template, callback));
                case TAKE -> this.takeEvents.add(new EventHandler(template, callback));
                default -> throw new RuntimeException("Unknow mode : " + mode);
            }

            this.mutex.unlock();
        } else {
            callback.call(tuple);
        }
    }

    public void save() {
        File saveFile = new File(SAVE_FILE_PATH);

        if (!saveFile.exists()) {
            try {
                saveFile.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        try {
            OutputStream os = new FileOutputStream(saveFile);
            ObjectOutputStream oos = new ObjectOutputStream(os);

            oos.writeObject(this.tuplesList);

            os.close();
            oos.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void debug(String prefix) {
        System.out.println(prefix);
    }

    private boolean existTuple(Tuple template) {
        for (Tuple tuple : this.tuplesList) {
            if (tuple.matches(template)) {
                return true;
            }
        }
        return false;
    }

    private Tuple getTuple(Tuple template) {
        Tuple tuple = null;

        for (Tuple t : this.tuplesList) {
            if (t.matches(template)) {
                tuple = t;
                break;
            }
        }

        if (tuple != null) {
            this.tuplesList.remove(tuple);
        }

        return tuple;
    }

    private Tuple copyTuple(Tuple template) {
        for (Tuple tuple : this.tuplesList) {
            if (tuple.matches(template)) {
                return tuple.deepclone();
            }
        }
        return null;
    }

    private void load() {
        File saveFile = new File(SAVE_FILE_PATH);

        if (!saveFile.exists())
            return;

        try {
            InputStream is = new FileInputStream(saveFile);
            ObjectInputStream ois = new ObjectInputStream(is);

            ArrayList<Tuple> tuples = (ArrayList<Tuple>) ois.readObject();
            System.out.println(tuples);
            this.tuplesList = tuples;

            is.close();
            ois.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private class EventHandler {

        private Tuple template;
        private Callback callback;

        public EventHandler(Tuple template, Callback callback) {
            this.template = template;
            this.callback = callback;
        }

        public boolean worksWith(Tuple tuple) {
            return tuple.matches(this.template);
        }

        public void callWith(Tuple tuple) {
            this.callback.call(tuple);
        }
    }
}
