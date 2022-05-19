package linda.server;

import linda.Callback;
import linda.Tuple;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class RemoteCallbackImpl extends UnicastRemoteObject implements RemoteCallback{

    private final Callback callback;

    public RemoteCallbackImpl(Callback callback) throws RemoteException {
        super();
        this.callback = callback;
    }


    @Override
    public void call(Tuple tuple) throws RemoteException {
        callback.call(tuple);
    }

}
