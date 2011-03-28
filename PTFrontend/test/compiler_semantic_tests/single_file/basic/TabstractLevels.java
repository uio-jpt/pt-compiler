template T1 {
    class X {
        tabstract String getString();
    }
}

template T2 {
    inst T1;

    class X adds {
        public void printString() {
            System.out.println( getString() );
        }
    }
}

package P {
    inst T2;

    class X adds {
        String getString() { return "Hello"; }

        public static void main(String args[]) {
            new X().printString();
        }
    }
}
