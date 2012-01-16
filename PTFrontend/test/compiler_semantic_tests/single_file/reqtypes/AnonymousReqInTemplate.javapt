template T {

    required type Foo {
        String s();
    } 

    class X {

        public void printFoo(Foo f) {
            System.out.println(f.s());
        }

        public void makeNewFooAndPrint() {
            /*
               Parallell mellom required types og abstrakte klasser?
               Prøver å opprette en ny anonym Foo, selv om dette er en required type.
               Følgende hadde vært gyldig java-syntaks selv hvis Foo hadde vært abstrakt:
             */
            Foo f = new Foo() { String s() { return "This was created anonymously"; } };
            printFoo(f);
        }
    }
}


package P {
    inst T with  Foo <= Bar;

    class Bar {
        String s() { return "nevermind right now"; }
    }


    class Main {
        public static void main(String args[]) {
            new X().makeNewFooAndPrint();
        }
    }
}
