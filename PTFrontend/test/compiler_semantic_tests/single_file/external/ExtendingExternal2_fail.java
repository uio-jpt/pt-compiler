template T {
    class V extends external Exception {
    }
}

package P {
    inst T with V => W; // fails: renaming anything inheriting from external is invalid

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
