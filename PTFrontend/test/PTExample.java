template T1 { // PTTemplate (PTDecl)
    class A {
        int x;
        double aa;
        A(double a) {
            aa = a;
        }
        A(boolean noConflict) {
        }
    }
    class B {
        String b;
        B(String bb) {
            b = bb;
        }
        B(boolean noConflict) {
        }
        int x2;
    }
}

package PackageTest1 {
    inst T1 with A => Y, B => Y;
    class Y adds {
        Y() {
            super[A](123.12);
            // super[B]("hei"); //TODO: fiks støtte for flere.
        }
        int ksdjf;
    }
 }

