template T <V> {
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
    inst T<String>;

    class X {
        public static void main(String args[]) {
            System.out.println( new A().setV("Hello world!").getV() );
        }
    }
}
