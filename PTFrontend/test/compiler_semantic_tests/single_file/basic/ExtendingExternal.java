package P {
    class W extends Exception {
        public void v() {
            System.out.println( "Hello world!" );
        }
    }

    class X {
        public static void main(String args[]) {
            System.out.println( new W().v() );
        }
    }
}
