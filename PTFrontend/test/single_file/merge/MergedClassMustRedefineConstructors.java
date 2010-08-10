template SimpleTemplate {

    class A {
        int x;
        A(int x) {
            this.x = x;
        }
    }

    class B {
        int y;
        
        B(int y) {
            this.y = y;
        }
    }
}


package AddPackage {
    inst SimpleTemplate with A => M, B => M;

    class M adds {
        
        M(int x, int y) {
            tsuper[A](x);
            tsuper[B](y);
        }
    }
}
