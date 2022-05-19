package linda.server;

import linda.Callback;
import linda.Linda;
import linda.Tuple;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collection;

/** Client part of a client/server implementation of Linda.
 * It implements the Linda interface and propagates everything to the server it is connected to.
 * */
public class LindaClient implements Linda {

    private LindaServer server;
	
    /** Initializes the Linda implementation.
     *  @param serverURI the URI of the server, e.g. "rmi://localhost:4000/LindaServer" or "//localhost:4000/LindaServer".
     */
    public LindaClient(String serverURI) throws RemoteException, NotBoundException, URISyntaxException, MalformedURLException {
        URI uri = new URI(serverURI);
        this.server = (LindaServer) Naming.lookup(serverURI);
    }

    @Override
    public void write(Tuple t) {
        try {
            this.server.write(t);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Tuple take(Tuple template) {
        try {
            return this.server.take(template);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Tuple read(Tuple template) {
        try {
            return this.server.read(template);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Tuple tryTake(Tuple template) {
        try {
            return this.server.tryTake(template);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Tuple tryRead(Tuple template) {
        try {
            return this.server.tryRead(template);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Collection<Tuple> takeAll(Tuple template) {
        try {
            return this.server.takeAll(template);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Collection<Tuple> readAll(Tuple template) {
        try {
            return this.server.readAll(template);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void eventRegister(eventMode mode, eventTiming timing, Tuple template, Callback callback) {
        try {
            RemoteCallback remoteCallback = new RemoteCallbackImpl(callback);
            this.server.eventRegister(mode, timing, template, remoteCallback);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void debug(String prefix) {
        try {
            this.server.debug(prefix);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

}
