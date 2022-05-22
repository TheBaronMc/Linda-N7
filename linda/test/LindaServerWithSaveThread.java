package linda.test;

import linda.server.LindaServer;
import linda.server.PrimaryLindaServer;

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class LindaServerWithSaveThread extends Thread {

    private int port;

    public LindaServerWithSaveThread(int port) {
        this.port = port;
    }


    public static void setTimeout(Runnable runnable, int delay){
        new Thread(() -> {
            try {
                Thread.sleep(delay);
                runnable.run();
            }
            catch (Exception e){
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

            LindaServer finalLindaServer = lindaServer;

            System.out.println("### LindaServer : Binded");

            setTimeout(new Runnable() {
                @Override
                public void run() {
                    try {
                        finalLindaServer.save();
                        System.out.println("Tuple saved !");
                    } catch (RemoteException e) {
                        throw new RuntimeException(e);
                    }
                }
            }, 15000);

        } catch (RemoteException e) {
            throw new RuntimeException(e);
        } catch (AlreadyBoundException e) {
            throw new RuntimeException(e);
        }
    }
}
