template T1 {
    class A { int f(int x) { return 5; } }
}

template T2 {
    class A { int f(int x) { return 15; } }
}

package P {
    inst T1 with A => X;
    inst T2 with A => X (f(int) -> ff);    

    class X adds {
        int x;

        int f(int x) {
            return tsuper[A].f(x); // f should be T1.A.f();
        }
    }
}

