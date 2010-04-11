template T1 { // PTTemplate (PTDecl)
    class A {
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
    inst T1 with A => Y;
    class C { }
    class B extends C {
        B() {
            super(123.12);
            //super[A](12.13);
            //int x = 10;
        }
    }
 }

