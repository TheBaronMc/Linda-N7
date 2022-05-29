package linda.test;

import linda.server.BackupLindaServer;
import linda.server.LindaServer;

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class StartBackupServer {
    public static void main(String[] args) throws RemoteException, AlreadyBoundException {
        LindaServer lindaServer = new BackupLindaServer("//localhost:4000/LindaServer");

        System.out.println("=== Démarrage du serveur ===");
        Registry dns = LocateRegistry.createRegistry(4001);

        dns.bind("LindaServer", lindaServer);

        System.out.println("### LindaServer : Binded");
    }
}
