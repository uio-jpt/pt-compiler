// TODO should this really fail?

template SimpleTemplate {

    class A {
        int getX() { return 533; }
    }

    class B {
        int getX() { return 133; }        
    }
}


package AddPackage {
    inst SimpleTemplate with A => M, B => M;

    class M adds {
        int getX() {
            // legal and required
            return tsuper[A].getX() + tsuper[B].getX();
        }

        int getY() {
            // error is here
            return tsuper[A].getX() + tsuper[B].getX();
        }
        
        
    }
}
