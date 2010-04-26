template T1 { // PTTemplate (PTDecl)
    class A {
        A(String s) { }
        int f() { return 3; }
        int g() { return 3; }
    }
    class B {
        B(int i) { }
        int f() { return 1; }
    }
}

package PackageTest1 {
    inst T1 with B => Z, A => Z;
    class Z adds {
        Z() {
            super[A]("hei");
            super[B](2);
        }
        int f() {
            return super[A].f() + super[B].f();
        }
    }
 }
