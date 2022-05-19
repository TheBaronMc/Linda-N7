package linda.shm;

import linda.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.CopyOnWriteArrayList;

/** Shared memory implementation of Linda. */
public class CentralizedLinda implements Linda {
    /**
     * Collection de tous les tuples ajoutés
     */
    private Collection<Tuple> tuples;
    /**
     * Collection de callbackHandlers générés par des "take"
     */
    private Collection<CallbackHandler> takeCallbacks;
    /**
     * Collection de callbackHandlers générés par des "read"
     */
    private Collection<CallbackHandler> readCallbacks;
    public CentralizedLinda() {
        this.tuples = new CopyOnWriteArrayList<>();
        this.takeCallbacks = new CopyOnWriteArrayList<>();
        this.readCallbacks = new CopyOnWriteArrayList<>();
    }

    @Override
    public void write(Tuple t) {
        // on débloque tous les read en attente en priorité
        for(CallbackHandler readCallback : this.readCallbacks){
            if(readCallback.doesMatch(t.deepclone())){
                this.readCallbacks.remove(readCallback);
                readCallback.call(t.deepclone());
            }
        }

        // on débloque un unique take en attente si possible
        for (CallbackHandler takeCallback : this.takeCallbacks ){
            if(takeCallback.doesMatch(t.deepclone())){
                this.takeCallbacks.remove(takeCallback);
                takeCallback.call(t.deepclone());
                // le tuple n'est pas écrit s'il débloque un take en attente.
                return;
            }
        }
        // on ajoute le tuple à la liste des tuples s'il ne match aucun take.
        this.tuples.add(t);
    }

    @Override
    public Tuple take(Tuple template) {
        CallbackMutex cb = new CallbackMutex();
        // On enregistre le read avec un callback associé au tuple recherché
        this.eventRegister(eventMode.TAKE, eventTiming.IMMEDIATE, template, cb);
        // On met le take en attente le temps que le callback soit appelé
        cb.sleep();
        // On renvoie le tuple une fois que le take est débloqué
        return cb.getTuple().deepclone();
    }

    @Override
    public Tuple read(Tuple template) {
        CallbackMutex cb = new CallbackMutex();
        // On enregistre le read avec un callback associé au tuple recherché
        this.eventRegister(eventMode.READ, eventTiming.IMMEDIATE, template, cb);
        // On met le read en attente le temps que le callback soit appelé
        cb.sleep();
        // On renvoie le tuple une fois que le read est débloqué
        return cb.getTuple().deepclone();
    }

    @Override
    public Tuple tryTake(Tuple template) {
        // On parcourt la collection de tous les tuples
        for(Tuple tuple : this.tuples){
            // on retourne le premier qui convient au template et on le supprime de la liste
            if(tuple.matches(template)){
                this.tuples.remove(tuple);
                return tuple.deepclone();
            }
        }
        // On retourne null si aucun tuple ne convient au template
        return null;
    }

    @Override
    public Tuple tryRead(Tuple template) {
        // On parcourt la collection de tous les tuples
        for(Tuple tuple : this.tuples){
            // on retourne le premier qui convient au template
            if(tuple.matches(template)){
                return tuple.deepclone();
            }
        }
        // On retourne null si aucun tuple ne convient au template
        return null;
    }

    @Override
    public Collection<Tuple> takeAll(Tuple template) {
        Collection<Tuple> matched = new ArrayList<>();
        List<Tuple> toRemove = new ArrayList<>();
        // On utilise un itérateur pour boucler sur la liste
        // pour pas que celle ci puisse être modifiée pendant l'opération
        ListIterator<Tuple> it = (ListIterator<Tuple>) this.tuples.iterator();
        Tuple tuple;
        while (it.hasNext()) {
            if ((tuple = it.next()).matches(template)) {
                matched.add(tuple.deepclone());
                // on construit la liste des éléments à supprimer
                toRemove.add(tuple);
            }
        }
        // on supprime tous les éléments en une seule opération (pour ne pas utiliser trop de mémoire)
        this.tuples.removeAll(toRemove);
        return matched;
    }

    @Override
    public Collection<Tuple> readAll(Tuple template) {
        Collection<Tuple> matched = new ArrayList<>();
        // On utilise un itérateur pour boucler sur la liste
        // pour pas que celle ci puisse être modifiée pendant l'opération
        ListIterator<Tuple> it = (ListIterator<Tuple>) this.tuples.iterator();
        Tuple tuple;
        while (it.hasNext()) {
            if ((tuple = it.next()).matches(template)) {
                matched.add(tuple.deepclone());
            }
        }
        return matched;
    }

    @Override
    public void eventRegister(eventMode mode, eventTiming timing, Tuple template, Callback callback) {
        Tuple tuple = null;
        // si le timing est immédiat on essaye de trouver un tuple sans que ce soit bloquant
        // pour pouvoir register l'event et le déclencher plus tard si on trouve rien
        if(eventTiming.IMMEDIATE == timing){
            if(eventMode.READ == mode){
                tuple = this.tryRead(template);
            }else if(eventMode.TAKE == mode){
                tuple = this.tryTake(template);
            }
        }
        // on a trouvé un tuple donc on appelle le call back immédiatement
        if(null != tuple){
            callback.call(tuple);
        }else{
            CallbackHandler callbackHandler = new CallbackHandler(template, callback);
            if(eventMode.READ == mode){
                this.readCallbacks.add(callbackHandler);
            }else if(eventMode.TAKE == mode){
                this.takeCallbacks.add(callbackHandler);
            }
        }
    }
    public void save(String filePath) {
        try {
            FileOutputStream fileWriter = new FileOutputStream(filePath);
            ObjectOutputStream objectWriter = new ObjectOutputStream(fileWriter);
            objectWriter.writeObject(this.tuples);
            objectWriter.close();
            fileWriter.close();
        } catch (IOException e) {
            System.err.println("Fatal IO error with " + filePath);
        }
    }

    /**
     * Lire l'espace de tuples à partir d'un fichier (désérialisation)
     * @param filePath chemin d'accès du fichier
     */
    public void load(String filePath) {
        try {
            FileInputStream fileReader = new FileInputStream(filePath);
            ObjectInputStream objectReader = new ObjectInputStream(fileReader);
            this.tuples = (CopyOnWriteArrayList<Tuple>) objectReader.readObject();
            objectReader.close();
            fileReader.close();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Fatal IO error with " + filePath);
        }
    }

    @Override
    public void debug(String prefix) {

    }
}
