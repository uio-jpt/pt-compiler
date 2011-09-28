template T1 {
    class A { }
    class B extends A { }
}

template T2 {
    class C { }
    class D extends C { }
}

template T3 {
    inst T1 with B => U, A => A;
    inst T2 with C => U, D => D;
    class U adds { }
    class V extends U { }
    class W extends V { }
}

package P {
    inst T3 with U => K, V => L, W => M;
    class K adds { } // C, B, U
    class L adds { }
    class M adds { }
    class N extends M { }
}

