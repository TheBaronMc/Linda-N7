package linda.test;

public class TwoWhiteboardTest {

    public static void main(String args[]) throws Exception {
        int port = 4000;
        String serverUri = "//localhost:4000/LindaServer";

        Thread server = new LindaServerThread(port);
        Thread client1 = new WhiteboardClient(serverUri, null);
        Thread client2 = new WhiteboardClient(serverUri, null);

        server.start();
        client1.start();
        client2.start();
    }

}
