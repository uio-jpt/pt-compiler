
template SimpleTemplate {

    class A {
        int k;
    }

    class B {
        int j;
    }
}


package AddPackage {
    inst SimpleTemplate with A => M, B => M;

    class M adds {
        M(int k, int j) {
            tsuper[A](k);
            tsuper[B](j);
        }
    }
}
