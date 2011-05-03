template T <V implements Runnable> {
    class A {
        V v;

        V getV() {
            return v;
        }

        public A setV(V v) {
            this.v = v;
            return this;
        }
    }
}

package P {
    inst T<W>; // illegal: W does not satisfy the constraints (does not implement Runnable)

    class W {
        public String getString() {
            return "Hello world!";
        }
    }

    class X {
        public static void main(String args[]) {
            System.out.println( new A().setV(new W()).getV().getString() );
        }
    }
}
