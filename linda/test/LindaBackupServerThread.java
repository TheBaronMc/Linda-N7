package linda.test;

import linda.server.BackupLindaServer;
import linda.server.LindaServer;

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class LindaBackupServerThread extends Thread {

    private int port;
    private String primaryServerUri;

    public LindaBackupServerThread(int port, String primaryServerUri) {
        this.port = port;
        this.primaryServerUri = primaryServerUri;
    }

    @Override
    public void run() {
        LindaServer lindaServer = null;
        try {
            lindaServer = new BackupLindaServer(primaryServerUri);

            System.out.println("=== Démarrage du serveur secondaire ===");
            Registry dns = LocateRegistry.createRegistry(port);

            dns.bind("LindaServer", lindaServer);

            LindaServer finalLindaServer = lindaServer;

            System.out.println("### LindaServer : Binded");
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        } catch (AlreadyBoundException e) {
            throw new RuntimeException(e);
        }
    }
}
