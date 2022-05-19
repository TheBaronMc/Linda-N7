package linda.server;

import linda.Callback;
import linda.Linda;
import linda.Tuple;
import linda.shm.CentralizedLinda;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collection;

public class LindaServerImpl extends UnicastRemoteObject implements LindaServer {

    private Linda linda;

    protected LindaServerImpl() throws RemoteException {
        linda = new CentralizedLinda();
    }

    @Override
    public void write(Tuple t) throws RemoteException {
        linda.write(t);
    }

    @Override
    public Tuple take(Tuple template) throws RemoteException {
        return linda.take(template);
    }

    @Override
    public Tuple read(Tuple template) throws RemoteException {
        return linda.read(template);
    }

    @Override
    public Tuple tryTake(Tuple template) throws RemoteException {
        return linda.tryTake(template);
    }

    @Override
    public Tuple tryRead(Tuple template) throws RemoteException {
        return linda.tryRead(template);
    }

    @Override
    public Collection<Tuple> takeAll(Tuple template) throws RemoteException {
        return linda.takeAll(template);
    }

    @Override
    public Collection<Tuple> readAll(Tuple template) throws RemoteException {
        return linda.readAll(template);
    }

    @Override
    public void eventRegister(Linda.eventMode mode, Linda.eventTiming timing, Tuple template, RemoteCallback callback) throws RemoteException {
        linda.eventRegister(mode, timing, template, new Callback() {
            @Override
            public void call(Tuple t) {
                try {
                    callback.call(t);
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    @Override
    public void debug(String prefix) throws RemoteException {

    }
}
