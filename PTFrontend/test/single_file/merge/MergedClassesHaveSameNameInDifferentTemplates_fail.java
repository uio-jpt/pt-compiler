template T1 {
    class A { int f() { return 5; } }
}

template T2 {
    class A { int f() { return 15; } }
}

package P {
    inst T1 with A => X;
    inst T2 with A => X;    

    class X adds {
        int x;

        int f() {
            return tsuper[A].f(); // which f?
        }
    }
}
