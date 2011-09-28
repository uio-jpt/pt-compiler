package P {
    class W extends Exception { // fails: must use "extends external"
    }

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
