package linda.server;

import linda.shm.CentralizedLinda;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class BackupLindaServer extends AbstractLindaServer {

    public BackupLindaServer(String primaryServerURI) throws RemoteException {
        super();

        linda = null;

        try {
            LindaServer primaryServer = (LindaServer) Naming.lookup(primaryServerURI);

            while (true) {
                linda = primaryServer.getState();
                Thread.sleep(5000);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            if (linda == null) {
                linda = new CentralizedLinda();
            }
        }

    }

}
