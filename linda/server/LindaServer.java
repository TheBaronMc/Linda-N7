package linda.server;

import linda.Linda;
import linda.Tuple;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Collection;

public interface LindaServer extends Remote {
    /** Adds a tuple t to the tuplespace. */
    void write(Tuple t) throws RemoteException;

    /** Returns a tuple that matches the template and removes it from the tuplespace.
     * Sleeps if none is found, until a matching tuple is added to the tuplespace and removes the latter right away.
     * */
    Tuple take(Tuple template) throws RemoteException;

    /** Returns a tuple matching the template and leaves it in the tuplespace.
     * Sleeps until a matching tuple is added to the tuplespace.
     * */
    Tuple read(Tuple template) throws RemoteException;

    /** Returns a tuple matching the template and removes it from the tuplespace.
     * Returns null if none is found.
     * */
    Tuple tryTake(Tuple template) throws RemoteException;

    /** Returns a tuple matching the template and leaves it in the tuplespace.
     * Returns null if none found.
     * */
    Tuple tryRead(Tuple template) throws RemoteException;

    /** Returns a collection of all the tuples matching the template and removes them from the tuplespace.
     * Returns an empty collection of tuples if none is found.
     */
    Collection<Tuple> takeAll(Tuple template) throws RemoteException;

    /** Returns all the tuples matching the template and leaves them in the tuplespace.
     * Returns an empty collection of tuples if none is found.
     */
    Collection<Tuple> readAll(Tuple template) throws RemoteException;

    /**
     * Renvoie un tuple correspond à un template, en take ou en read, immédiatement ou dans le futur
     * Utilise un sémaphore pour attendre qu'un tuple ait été lu ou pris
     * @param mode read ou take
     * @param timing mode immediate ou future
     * @param template template de tuple à rechercher
     * @return un tuple lu / pris correspondant au template
     * @throws RemoteException
     */
    Tuple waitEvent(Linda.eventMode mode, Linda.eventTiming timing, Tuple template) throws RemoteException;

    /** To debug, prints any information it wants (e.g. the tuples in tuplespace or the registered callbacks), prefixed by <code>prefix</code. */
    void debug(String prefix) throws RemoteException;

    /**
     * Saves the content of the tuplespace in a file. (serializing)
     * @param filePath file path
     */
    void save(String filePath) throws RemoteException;

    /**
     * reads the tuplespace content from a file (reverse serializing)
     * @param filePath file path
     */
    void load(String filePath) throws RemoteException;
}
