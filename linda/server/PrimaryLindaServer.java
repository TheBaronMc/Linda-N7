package linda.server;

import linda.shm.CentralizedLinda;

import java.rmi.RemoteException;

public class PrimaryLindaServer extends AbstractLindaServer {

    public PrimaryLindaServer() throws RemoteException {
        super();
        linda = new CentralizedLinda();
    }

}
