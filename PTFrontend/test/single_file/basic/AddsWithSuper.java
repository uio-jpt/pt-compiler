template T1 {

    class A {
        int a;
    }

    class B extends A {
        int b;
    }
}

template T2 {

    class C {
        int c;
    }

    class D extends C {
        int d;
    }
}

template T3 {
    inst T1 with B => U;
    inst T2 with C => U;

    class V extends U {
        int v;
    }

    class W extends V {
        int w;
    }
}

package P {
    inst T3 with U => K,
        V => L,
        W => M;

    class N extends M {
        int n;
    }
}
