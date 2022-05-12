package linda.shm;

import linda.Callback;
import linda.Linda;
import linda.Tuple;

import java.util.*;
import java.util.concurrent.Semaphore;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;

/** Shared memory implementation of Linda. */
public class CentralizedLinda implements Linda {

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
    }

    @Override
    public void write(Tuple t) {
        this.mutex.lock();

        ArrayList<Tuple> found;

        // Read Callbacks
        for (EventHandler event : this.readEvents) {
            if (event.worksWith(t)) {
                event.callWith(t.deepclone());
                this.readEvents.remove(event);
            }
        }

        // Take Callbacks
        for (EventHandler event : takeEvents) {
            if (event.worksWith(t)) {
                event.callWith(t);
                this.takeEvents.remove(event);
                this.mutex.unlock();
                return;
            }
        }

        this.tuplesList.add(t);

        this.readCondition.signalAll();
        this.takeCondition.signalAll();

        this.mutex.unlock();
    }

    @Override
    public Tuple take(Tuple template) {
        this.mutex.lock();

        while (!existTuple(template)) {
            try {
                this.takeCondition.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        Tuple tuple = this.getTuple(template);

        this.mutex.unlock();

        return tuple;
    }

    @Override
    public Tuple read(Tuple template) {
        this.mutex.lock();

        while (!this.existTuple(template)) {
            try {
                this.readCondition.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        Tuple tuple = this.copyTuple(template);

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

    @Override
    public void debug(String prefix) {
        System.out.println("================ DEBUG ================");

        System.out.println("------------- TUPLESPACE --------------");

        for (Tuple tuple : this.tuplesList)
            System.out.println(tuple);


        System.out.println("================= END =================");
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
        for (Tuple tuple : this.tuplesList) {
            if (tuple.matches(template)) {
                this.tuplesList.remove(tuple);
                return tuple;
            }
        }
        return null;
    }

    private Tuple copyTuple(Tuple template) {
        for (Tuple tuple : this.tuplesList) {
            if (tuple.matches(template)) {
                return tuple.deepclone();
            }
        }
        return null;
    }

    private class EventHandler {

        private Tuple template;
        private Callback callback;

        public EventHandler(Tuple template, Callback callback) {
            this.template = template;
            this.callback = callback;
        }

        public boolean worksWith(Tuple tuple) {
            return this.template.contains(tuple);
        }

        public void callWith(Tuple tuple) {
            this.callback.call(tuple);
        }
    }
}
