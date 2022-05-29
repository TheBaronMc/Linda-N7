package linda.test;

import linda.Tuple;
import linda.server.LindaClient;

public class BackupServerTest {

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

    public static void main(String args[]) throws Exception {
        LindaClient client = new LindaClient("//localhost:4000/LindaServer", "//localhost:4001/LindaServer");

        client.write(new Tuple(1, 2));

        client.write(new Tuple(2, 3));

        client.write(new Tuple(4, 5));

        Thread.sleep(15000);

        Tuple template = new Tuple(Integer.class, Integer.class);

        Tuple t = null;
        t = client.tryTake(template);
        System.out.println(t);
        t = client.tryTake(template);
        System.out.println(t);
        t=client.tryTake(template);
        System.out.println(t);
    }

}
