package linda.test;

import linda.*;

import java.util.Collection;

public class BasicTestTakeAll {

    public static void main(String[] a) {

        final Linda linda = new linda.shm.CentralizedLinda();
        // final Linda linda = new linda.server.LindaClient("//localhost:4000/aaa");

        new Thread() {
            public void run() {
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Tuple motif = new Tuple(Integer.class, Integer.class);
                Collection<Tuple> res = linda.takeAll(motif);
                System.out.println("(4) Resultat takeAll:" + res);
                linda.debug("(4)");
            }
        }.start();

        new Thread() {
            public void run() {
                try {
                    Thread.sleep(1700);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Tuple motif = new Tuple(Integer.class, Integer.class);
                Collection<Tuple> res = linda.takeAll(motif);
                System.out.println("(5) Resultat takeAll:" + res);
                linda.debug("(5)");
            }
        }.start();

        new Thread() {
            public void run() {
                try {
                    Thread.sleep(1200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                Tuple t1 = new Tuple(4, 5);
                System.out.println("(2) write: " + t1);
                linda.write(t1);

                Tuple t11 = new Tuple(4, 5);
                System.out.println("(2) write: " + t11);
                linda.write(t11);

                Tuple t12 = new Tuple(72, 56);
                System.out.println("(2) write: " + t12);
                linda.write(t12);

                Tuple t2 = new Tuple("hello", 15);
                System.out.println("(2) write: " + t2);
                linda.write(t2);

                Tuple t3 = new Tuple(4, "foo");
                System.out.println("(2) write: " + t3);
                linda.write(t3);

                linda.debug("(2)");

            }
        }.start();

    }
}
