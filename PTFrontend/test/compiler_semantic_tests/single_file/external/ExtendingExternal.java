package P {
    class W extends external Exception {
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
