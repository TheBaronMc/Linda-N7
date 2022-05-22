package linda.server;

import linda.Callback;
import linda.Linda;
import linda.Tuple;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collection;

/** Client part of a client/server implementation of Linda.
 * It implements the Linda interface and propagates everything to the server it is connected to.
 * */
public class LindaClient implements Linda {

    private LindaServer server;

    private String backupServerUri;
    private boolean connectedToBackup;
	
    /** Initializes the Linda implementation.
     *  @param serverURI the URI of the server, e.g. "rmi://localhost:4000/LindaServer" or "//localhost:4000/LindaServer".
     */
    public LindaClient(String primaryServerURI, String backupServerUri) {
        connectedToBackup = false;

        try {
            connectTo(primaryServerURI);
        } catch (Exception e) {
            try {
                e.printStackTrace();
                connectTo(backupServerUri);
                connectedToBackup = true;
            } catch (Exception e2) {
                throw new RuntimeException(e2);
            }
        }
    }

    @Override
    public void write(Tuple t) {
        try {
            this.server.write(t);
        } catch (RemoteException e) {
            try {
                if (!connectedToBackup) {
                    e.printStackTrace();
                    connectTo(backupServerUri);
                    connectedToBackup = true;

                    this.server.write(t);
                } else {
                    throw new RuntimeException(e);
                }

            } catch (Exception e2) {
                throw new RuntimeException(e2);
            }
        }
    }

    @Override
    public Tuple take(Tuple template) {
        try {
            return this.server.take(template);
        } catch (RemoteException e) {
            try {
                if (!connectedToBackup) {
                    e.printStackTrace();
                    connectTo(backupServerUri);
                    connectedToBackup = true;

                    return this.server.take(template);
                } else {
                    throw new RuntimeException(e);
                }

            } catch (Exception e2) {
                throw new RuntimeException(e2);
            }
        }
    }

    @Override
    public Tuple read(Tuple template) {
        try {
            return this.server.read(template);
        } catch (RemoteException e) {
            try {
                if (!connectedToBackup) {
                    e.printStackTrace();
                    connectTo(backupServerUri);
                    connectedToBackup = true;

                    return this.server.read(template);
                } else {
                    throw new RuntimeException(e);
                }

            } catch (Exception e2) {
                throw new RuntimeException(e2);
            }
        }
    }

    @Override
    public Tuple tryTake(Tuple template) {
        try {
            return this.server.tryTake(template);
        } catch (RemoteException e) {
            try {
                if (!connectedToBackup) {
                    e.printStackTrace();
                    connectTo(backupServerUri);
                    connectedToBackup = true;

                    return this.server.tryTake(template);
                } else {
                    throw new RuntimeException(e);
                }

            } catch (Exception e2) {
                throw new RuntimeException(e2);
            }
        }
    }

    @Override
    public Tuple tryRead(Tuple template) {
        try {
            return this.server.tryRead(template);
        } catch (RemoteException e) {
            try {
                if (!connectedToBackup) {
                    e.printStackTrace();
                    connectTo(backupServerUri);
                    connectedToBackup = true;

                    return this.server.tryRead(template);
                } else {
                    throw new RuntimeException(e);
                }

            } catch (Exception e2) {
                throw new RuntimeException(e2);
            }
        }
    }

    @Override
    public Collection<Tuple> takeAll(Tuple template) {
        try {
            return this.server.takeAll(template);
        } catch (RemoteException e) {
            try {
                if (!connectedToBackup) {
                    e.printStackTrace();
                    connectTo(backupServerUri);
                    connectedToBackup = true;

                    return this.server.takeAll(template);
                } else {
                    throw new RuntimeException(e);
                }

            } catch (Exception e2) {
                throw new RuntimeException(e2);
            }
        }
    }

    @Override
    public Collection<Tuple> readAll(Tuple template) {
        try {
            return this.server.readAll(template);
        } catch (RemoteException e) {
            try {
                if (!connectedToBackup) {
                    e.printStackTrace();
                    connectTo(backupServerUri);
                    connectedToBackup = true;

                    return this.server.readAll(template);
                } else {
                    throw new RuntimeException(e);
                }

            } catch (Exception e2) {
                throw new RuntimeException(e2);
            }
        }
    }

    @Override
    public void eventRegister(eventMode mode, eventTiming timing, Tuple template, Callback callback) {
        try {
            RemoteCallback remoteCallback = new RemoteCallbackImpl(callback);
            this.server.eventRegister(mode, timing, template, remoteCallback);
        } catch (RemoteException e) {
            try {
                if (!connectedToBackup) {
                    e.printStackTrace();
                    connectTo(backupServerUri);
                    connectedToBackup = true;

                    RemoteCallback remoteCallback = new RemoteCallbackImpl(callback);
                    this.server.eventRegister(mode, timing, template, remoteCallback);
                } else {
                    throw new RuntimeException(e);
                }

            } catch (Exception e2) {
                throw new RuntimeException(e2);
            }
        }
    }

    @Override
    public void debug(String prefix) {
        try {
            this.server.debug(prefix);
        } catch (RemoteException e) {
            try {
                if (!connectedToBackup) {
                    e.printStackTrace();
                    connectTo(backupServerUri);
                    connectedToBackup = true;

                    this.server.debug(prefix);
                } else {
                    throw new RuntimeException(e);
                }

            } catch (Exception e2) {
                throw new RuntimeException(e2);
            }
        }
    }

    private void connectTo(String serverUri) throws MalformedURLException, NotBoundException, RemoteException {
        this.server = (LindaServer) Naming.lookup(serverUri);
    }

}
