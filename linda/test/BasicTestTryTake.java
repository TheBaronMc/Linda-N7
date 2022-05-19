package linda.test;

import linda.*;

public class BasicTestTryTake {

    public static void main(String[] a) {

        final Linda linda = new linda.shm.CentralizedLinda();
        // final Linda linda = new linda.server.LindaClient("//localhost:4000/aaa");

        new Thread() {
            public void run() {
                try {
                    Thread.sleep(2);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Tuple motif = new Tuple(Integer.class, String.class);
                Tuple res = linda.tryTake(motif);
                System.out.println("(1) Resultat TryTake:" + res);
                linda.debug("(1)");
            }
        }.start();

        new Thread() {
            public void run() {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Tuple motif = new Tuple(Integer.class, String.class);
                Tuple res = linda.tryTake(motif);
                System.out.println("(3) Resultat tryTake:" + res);
                linda.debug("(3)");
            }
        }.start();

        new Thread() {
            public void run() {
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Tuple motif = new Tuple(Integer.class, String.class);
                Tuple res = linda.tryTake(motif);
                System.out.println("(4) Resultat tryTake:" + res);
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
                Tuple motif = new Tuple(Integer.class, String.class);
                Tuple res = linda.tryTake(motif);
                System.out.println("(5) Resultat tryTake:" + res);
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

