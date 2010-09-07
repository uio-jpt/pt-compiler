template T1 {

    class A {
        int a;
    }

    class B extends A {
        int b;
    }
}

template T2 {

    class X {
        int x;
    }

    class Y extends X {
        int y;
    }
}

package AddPackage {
    inst T1 with A => J, B => K;
    inst T2 with X => J, Y => K;    
}
