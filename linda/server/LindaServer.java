package linda.server;

import linda.AsynchronousCallback;
import linda.Callback;
import linda.Linda;
import linda.Tuple;
import linda.shm.CentralizedLinda;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Collection;

public interface LindaServer extends Remote {

    public void write(Tuple t) throws RemoteException;

    public Tuple take(Tuple template) throws RemoteException;

    public Tuple read(Tuple template) throws RemoteException;

    public Tuple tryTake(Tuple template) throws RemoteException;

    public Tuple tryRead(Tuple template) throws RemoteException;

    Collection<Tuple> takeAll(Tuple template) throws RemoteException;

    Collection<Tuple> readAll(Tuple template) throws RemoteException;

    void save() throws RemoteException;

    void eventRegister(Linda.eventMode mode, Linda.eventTiming timing, Tuple template, RemoteCallback callback) throws RemoteException;

    void debug(String prefix) throws RemoteException;

    CentralizedLinda getState() throws RemoteException;
}
