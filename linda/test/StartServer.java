package linda.test;

import linda.server.LindaServer;
import linda.server.PrimaryLindaServer;

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class StartServer {
    public static void main(String[] args) throws RemoteException, AlreadyBoundException {
        LindaServer lindaServer = new PrimaryLindaServer();

        System.out.println("=== Démarrage du serveur ===");
        Registry dns = LocateRegistry.createRegistry(4000);

        dns.bind("LindaServer", lindaServer);

        System.out.println("### LindaServer : Binded");
    }
}
