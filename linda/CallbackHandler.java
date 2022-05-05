package linda;

public class CallbackHandler {
    private Tuple template;
    private Callback callback;

    public CallbackHandler(Tuple template, Callback callback){
        this.template = template;
        this.callback = callback;
    }

    public void call(Tuple tuple){
        this.callback.call(tuple);
    }

    public boolean doesMatch(Tuple tuple){
        return tuple.matches(this.template);
    }
}
