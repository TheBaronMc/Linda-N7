package linda.server;

import linda.whiteboard.WhiteboardModel;
import linda.whiteboard.WhiteboardView;

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.*;

public class StartServer {

    public static void main(String args[]) throws Exception {
        Thread server = new Thread() {
          @Override
          public void run() {
              LindaServer lindaServer = null;
              try {
                  lindaServer = new LindaServerImpl();

                  System.out.println("=== Démarrage du serveur ===");
                  Registry dns = LocateRegistry.createRegistry(4000);

                  dns.bind("LindaServer", lindaServer);
                  System.out.println("### LindaServer : Binded");
              } catch (RemoteException e) {
                  throw new RuntimeException(e);
              } catch (AlreadyBoundException e) {
                  throw new RuntimeException(e);
              }
          }
        };

        if (args.length != 1) {
            System.err.println("Whiteboard serverURI.");
            return;
        }

        Thread client1 = new Thread() {
            @Override
            public void run() {
                LindaServer lindaServer = null;
                WhiteboardModel model = new WhiteboardModel();
                WhiteboardView view = new WhiteboardView(model);
                model.setView(view);
                model.start(new LindaClient("//localhost:4000/LindaServer"));
            }
        };

        Thread client2 = new Thread() {
            @Override
            public void run() {
                LindaServer lindaServer = null;
                WhiteboardModel model = new WhiteboardModel();
                WhiteboardView view = new WhiteboardView(model);
                model.setView(view);
                model.start(new LindaClient("//localhost:4000/LindaServer"));
            }
        };

        server.start();
        client1.start();
        client2.start();
    }

}
