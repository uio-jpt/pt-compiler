template T {
    class V extends external Exception {
    }

    class W extends V {
        public void foo() {
        }
    }
}

package P {
    inst T with W => Y;

    class X {
        public static void main(String args[]) {
            try {
                throw new Y();
            }
            catch( Exception e ) {
                System.out.println( "ok!" );
            }
        }
    }
}
