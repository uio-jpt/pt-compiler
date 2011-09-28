template T {
    class W extends external Exception {
    }
}

package P {
    inst T;

    class X {
        public static void main(String args[]) {
            try {
                throw new W();
            }
            catch( Exception e ) {
                System.out.println( "ok!" );
            }
        }
    }
}
