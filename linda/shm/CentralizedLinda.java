package linda.shm;

import linda.Callback;
import linda.Linda;
import linda.Tuple;

import java.util.*;
import java.util.concurrent.Semaphore;

/** Shared memory implementation of Linda. */
public class CentralizedLinda implements Linda {

    private ArrayList<Tuple> tuplesList;
    private HashMap<Tuple, Callback> readCallbacks;
    private HashMap<Tuple, Callback> takeCallbacks;

    private int inReadQueue;
    private Semaphore readQueue;
    private int inTakeQueue;
    private Semaphore takeQueue;

    private Semaphore mutex;

    public CentralizedLinda() {
        this.tuplesList = new ArrayList<>();
        this.readCallbacks = new HashMap<>();
        this.takeCallbacks = new HashMap<>();

        this.inReadQueue = 0;
        this.readQueue = new Semaphore(0);
        this.inTakeQueue = 0;
        this.takeQueue = new Semaphore(0);

        this.mutex = new Semaphore(1);
    }

    @Override
    public void write(Tuple t) {
        try {
            this.mutex.acquire();

            // Read Callbacks
            for(Map.Entry<Tuple, Callback> entry : this.readCallbacks.entrySet()) {
                Tuple template = entry.getKey();

                if (t.matches(template)) {
                    Callback callback = entry.getValue();
                    callback.call(new Tuple(t));
                }
            }

            Set<Tuple> takeTemplates = this.takeCallbacks.keySet();
            for (Tuple template : takeTemplates) {
                if (t.matches(template)) {
                    Callback callback = this.takeCallbacks.get(template);
                    callback.call(t);
                    return;
                }
            }

            this.tuplesList.add(t);

            this.readQueue.release(this.inReadQueue);
            this.inReadQueue = 0;
            this.takeQueue.release(this.inTakeQueue);
            this.inTakeQueue = 0;

            this.mutex.release();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Tuple take(Tuple template) {
        try {
            this.mutex.acquire();

            while (!existTuple(template)) {
                this.inTakeQueue++;
                this.mutex.release();
                this.takeQueue.acquire();
                this.mutex.acquire();
            }

            Tuple tuple = this.getTuple(template);

            this.mutex.release();
            return tuple;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Tuple read(Tuple template) {
        try {
            this.mutex.acquire();

            while (!existTuple(template)) {
                this.inReadQueue++;
                this.mutex.release();
                this.readQueue.acquire();
                this.mutex.acquire();
            }

            Tuple tuple = this.copyTuple(template);

            this.mutex.release();
            return tuple;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Tuple tryTake(Tuple template) {
        try {
            this.mutex.acquire();
            Tuple tuple = this.getTuple(template);
            this.mutex.release();

            return tuple;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Tuple tryRead(Tuple template) {
        try {
            this.mutex.acquire();
            Tuple tuple = this.copyTuple(template);
            this.mutex.release();

            return tuple;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Collection<Tuple> takeAll(Tuple template) {
        try {
            ArrayList<Tuple> col = new ArrayList<>();

            this.mutex.acquire();

            while (!this.existTuple(template)) {
                col.add(this.getTuple(template));
            }

            this.mutex.release();

            return col;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Collection<Tuple> readAll(Tuple template) {
        try {
            ArrayList<Tuple> col = new ArrayList<>();

            this.mutex.acquire();

            for (Tuple tuple : this.tuplesList) {
                if (tuple.matches(template)) {
                    col.add(new Tuple(tuple));
                }
            }

            this.mutex.release();

            return col;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
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
            try {
                this.mutex.acquire();

                switch (mode) {
                    case READ -> this.readCallbacks.put(template, callback);
                    case TAKE -> this.takeCallbacks.put(template, callback);
                    default -> throw new RuntimeException("Unknow mode : " + mode);
                }

                this.mutex.release();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        } else {
            callback.call(tuple);
        }
    }

    @Override
    public void debug(String prefix) {
        System.out.println("================ DEBUG ================");

        System.out.println("------------- TUPLESPACE --------------");

        for (int i = 0; i < this.tuplesList.size(); i++)
            System.out.println(this.tuplesList.get(i));


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
                return new Tuple(tuple);
            }
        }
        return null;
    }

}
