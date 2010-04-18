template T1 { // PTTemplate (PTDecl)
    class A {
        int aLocal;
        A(int a) {
            aLocal = a;
        }
    }
    class B extends A {
        String b;
        B(String bb) {
            super(123);
            b = bb;
        }
    }
}

package PackageTest1 {
    inst T1 with A => Y, B => Y;
    //inst T1 with A => AA, B => BB; // FIX: denne vil knekke ettersom BB ikke subklasser etter rewriting.
    class Y adds {
        Y() {
            super[A](123.12);
            // super[B]("hei"); //TODO: fiks støtte for flere.
        }
    }
 }

