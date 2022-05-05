package linda.shm;

import linda.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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
            if(readCallback.doesMatch(t)){
                this.readCallbacks.remove(readCallback);
                readCallback.call(t);
            }
        }

        // on débloque un unique take en attente si possible
        for (CallbackHandler takeCallback : this.takeCallbacks ){
            if(takeCallback.doesMatch(t)){
                this.takeCallbacks.remove(takeCallback);
                takeCallback.call(t);
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
        return cb.getTuple();
    }

    @Override
    public Tuple read(Tuple template) {
        CallbackMutex cb = new CallbackMutex();
        // On enregistre le read avec un callback associé au tuple recherché
        this.eventRegister(eventMode.READ, eventTiming.IMMEDIATE, template, cb);
        // On met le read en attente le temps que le callback soit appelé
        cb.sleep();
        // On renvoie le tuple une fois que le read est débloqué
        return cb.getTuple();
    }

    @Override
    public Tuple tryTake(Tuple template) {
        // On parcourt la collection de tous les tuples
        for(Tuple tuple : this.tuples){
            // on retourne le premier qui convient au template et on le supprime de la liste
            if(tuple.matches(template)){
                this.tuples.remove(tuple);
                return tuple;
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
                return tuple;
            }
        }
        // On retourne null si aucun tuple ne convient au template
        return null;
    }

    @Override
    public Collection<Tuple> takeAll(Tuple template) {
        List<Tuple> matched = new ArrayList<>();
        //On parcourt la liste des tuples
        for(Tuple tuple : this.tuples){
            // On ajoute le tuple à la liste s'il convient au template
            if(tuple.matches(template)){
                // Le tuple matché est supprimé de la collection de tuples
                this.tuples.remove(tuple);
                matched.add(tuple);
            }
        }
        return matched;
    }

    @Override
    public Collection<Tuple> readAll(Tuple template) {
        List<Tuple> matched = new ArrayList<>();
        //On parcourt la liste des tuples
        for(Tuple tuple : this.tuples){
            // On ajoute le tuple à la liste s'il convient au template
            if(tuple.matches(template)){
                matched.add(tuple);
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

    @Override
    public void debug(String prefix) {

    }

    // TO BE COMPLETED

}
