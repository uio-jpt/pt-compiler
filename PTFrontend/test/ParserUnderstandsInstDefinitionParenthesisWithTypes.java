template SimpleTemplate {
    
    class A {
        int k;

        A getA(A k) {
            return k;
        }
    }
}

package SimplePackage {
    inst SimpleTemplate with A => B (getA(A) -> getB);
}

