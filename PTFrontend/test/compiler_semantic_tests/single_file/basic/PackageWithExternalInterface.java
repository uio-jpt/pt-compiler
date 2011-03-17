template T {
    class TA implements Runnable {
        TA() {
        }

        public void run() {
            System.out.println( "OK!" );
        }
    }
}

package P {
    inst T;
    class R {
        void testrunner() {
            Runnable t = new TA();
            t.run();
        }
    }
}
