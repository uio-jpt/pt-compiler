template T {
    class X {
        private class Y {
            public void callPrintMessage() {
                new X().printMessage();
            }
        }

        public void printMessage() {
            System.out.println( "Hello world!" );
        }

        public void indirectPrintMessage() {
            new Y().callPrintMessage();
        }
    }
}

package P {
    inst T with X => XX ( printMessage() -> f );
}