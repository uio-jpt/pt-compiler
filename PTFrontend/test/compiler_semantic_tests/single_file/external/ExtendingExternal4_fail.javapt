template T {
    class V extends external RuntimeException {
    }
}

template U {
    class V extends external UserException {
    }
}

package P {
    inst T;
    inst U;
    // fails: merging classes inheriting from different external

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
