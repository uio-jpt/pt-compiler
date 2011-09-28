template T <V extends Runnable> {
    class A {
        V v;

        public V getV() {
            return v;
        }

        public A setV(V v) {
            this.v = v;
            return this;
        }
    }
}

package P {
    inst T<W>;

    class W implements Runnable {
        public void run() {
            System.out.println( "Hello world!" );
        }
    }

    class X {
        public static void main(String args[]) {
            new A().setV( new W() ).getV().run();
        }
    }
}
