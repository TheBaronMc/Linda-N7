package linda.test;

import linda.server.LindaClient;
import linda.server.LindaServer;
import linda.whiteboard.WhiteboardModel;
import linda.whiteboard.WhiteboardView;

public class WhiteboardClient extends Thread {

    private String primaryServerUri;
    private String backupServerUri;

    public WhiteboardClient(String primaryServerURI, String backupServerURI) {
        this.primaryServerUri = primaryServerURI;
        this.backupServerUri = backupServerURI;
    }

    @Override
    public void run() {
        LindaServer lindaServer = null;
        WhiteboardModel model = new WhiteboardModel();
        WhiteboardView view = new WhiteboardView(model);
        model.setView(view);
        model.start(new LindaClient(primaryServerUri, backupServerUri));
    }

}
