template T {

    class A { int f() { return 1; } }
    class B { int f; }

}

package P {
    inst T with A => X, B => X;

    class X adds { }
}