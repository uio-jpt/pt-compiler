template T1 { // PTTemplate (PTDecl)
    class A {
        int f() { return 3; }
    }
    class B {
        int f() { return 1; }
    }
}

package PackageTest1 {
    inst T1 with B => Z, A => Z;
    class Z adds {
        Z() {
            super[A]("hei");
            super[B]("hei");
            super[A].f("hei");
            super[B].g("hei");
        }
        int f() {
            return 123;
        }
    }
 }
