package wind.test_usb;

public class test2 extends Thread {

    @Override
    public void run() {
        for (int i = 0; i < 100; i++) {
            System.out.println(Thread.currentThread().getName() + " " + i);

        }
    }

    public static void main(String[] args) {
        Thread t1 = new Thread();
        t1.start();

    }

}