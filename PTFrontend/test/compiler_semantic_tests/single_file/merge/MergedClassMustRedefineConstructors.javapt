template SimpleTemplate {

    class A {
        int x;
        A() {
            this.x = 2;
        }
    }

    class B {
        int y;
        
        B() {
            this.y = 3;
        }
    }
}


package AddPackage {
    inst SimpleTemplate with A => M, B => M;

    class M adds {
        
        M(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
}
