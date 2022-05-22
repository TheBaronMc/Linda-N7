package linda.test;

public class SaveWhiteboardTest {

    public static void main(String args[]) throws Exception {
        int port = 4000;
        String serverUri = "//localhost:4000/LindaServer";

        Thread server = new LindaServerWithSaveThread(port);
        Thread client1 = new WhiteboardClient(serverUri, null);

        server.start();
        client1.start();
    }

}
