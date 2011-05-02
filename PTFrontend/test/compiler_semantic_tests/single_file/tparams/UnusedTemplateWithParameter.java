template T <V> {
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
    class X {
        public static void main(String args[]) {
            System.out.println( "Hello world!" );
        }
    }
}
