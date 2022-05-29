package linda.test;

import linda.server.LindaServer;
import linda.server.PrimaryLindaServer;

import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class LindaServerThread extends Thread {

    private int port;

    public LindaServerThread(int port) {
        this.port = port;
    }

    public static void setTimeout(Runnable runnable, int delay){
        new Thread(() -> {
            try {
                Thread.sleep(delay);
                runnable.run();
            } catch (Exception e){
                System.err.println(e);
            }
        }).start();
    }

    @Override
    public void run() {
        LindaServer lindaServer = null;
        try {
            lindaServer = new PrimaryLindaServer();

            System.out.println("=== Démarrage du serveur ===");
            Registry dns = LocateRegistry.createRegistry(port);

            dns.bind("LindaServer", lindaServer);

            System.out.println("### LindaServer : Binded");
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        } catch (AlreadyBoundException e) {
            throw new RuntimeException(e);
        }
    }


}
