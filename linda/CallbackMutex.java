package linda;

import java.util.concurrent.Semaphore;

public class CallbackMutex implements Callback{

    private Tuple tuple;
    private Semaphore mutex;

    public CallbackMutex(){
        this.mutex = new Semaphore(0);
    }

    public Tuple getTuple(){
        return this.tuple;
    }

    public void sleep(){
        try {
            this.mutex.acquire();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void call(Tuple t) {
        this.tuple = t;
        this.mutex.release();
    }
}
