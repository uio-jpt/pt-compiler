template T {
    class V extends external Exception {
    }
}

template U {
    class V extends external Exception {
    }
}

package P {
    inst T;
    inst U;

    class X {
        public static void main(String args[]) {
            try {
                throw new V();
            }
            catch( Exception e ) {
                System.out.println( "ok!" );
            }
        }
    }
}
