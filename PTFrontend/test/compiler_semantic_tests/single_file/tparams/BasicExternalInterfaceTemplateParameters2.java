template T <class V implements Runnable> {
    class A {
        V v;

        V getV() {
            return v;
        }

        A(V v) {
            this.v = v;
        }
    }
}

package P {
    inst T<W>;

    class W implements Runnable {
        public void run() {
            System.out.println( "Hello world!" );
        }
    };

    class X {
        public static void main(String args[]) {
            new A( new W() ).getV().run();
            System.out.println( new A(new W()).getString() );
        }
    }
}
