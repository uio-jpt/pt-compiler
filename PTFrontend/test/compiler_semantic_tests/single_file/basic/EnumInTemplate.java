template T {
    enum Test {
		FOO, BAR, KAKE, RISKOKER;
    }
}

package P  {
    inst T;

    class V {
        public static void main(String args[]) {
            Test y = RISKOKER;
            switch( y ) {
                case FOO: System.out.println( "foo" ); break;
                case BAR: System.out.println( "bar" ); break;
                case KAKE: System.out.println( "kake" ); break;
                case RISKOKER: System.out.println( "riskoker" ); break;
            }
        }
    }

}
    
