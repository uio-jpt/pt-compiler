template SimpleTemplate {
    
    class A {
        int k;

        A getA(int x) {
            return new A();
        }
    }

    class B {

        A getA(int x) {
            return null;
        }
    }
}

package SimplePackage {
    // collision if not (*) is working.
    inst SimpleTemplate with A => B (getA(*) -> getB);
}