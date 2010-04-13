template T1 { // PTTemplate (PTDecl)
    class A {
        int x;
        /*
        float localA;
        A(float y) {
            super();
            localA = y;
        }
        */
    }
    class B {
        int x2;
        /*
        float localA;
        A(float y) {
            super();
            localA = y;
        }
        */
    }
}

package PackageTest1 { //PTPackage (PTDecl)
    inst T1 with A => Y, B => Y;
    class X { Integer a;}
    class Z extends X {
        Z() {
            //            super(123.12);
            //super[A](12.13);
            //int x = 10;
        }
    }
    class Y adds {
        int ksdjf;
    }
 }

