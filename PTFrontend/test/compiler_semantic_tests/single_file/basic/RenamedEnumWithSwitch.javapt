template T {
    enum Test {
		FOO, BAR, KAKE, RISKOKER;
    }
}

package P  {
    inst T with Test => MyEnum;

    class V {
        public static void main(String args[]) {
            MyEnum y = MyEnum.RISKOKER;
            switch( y ) {
                case KAKE: System.out.println( "kake" ); break;
                default: System.out.println( "ingen kake" ); break;
            }
        }
    }

}
    
