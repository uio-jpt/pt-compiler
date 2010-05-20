template SimpleTemplate {
    
    class A {
        int k;


        A k() {
            return null;
        }
    }
}

package SimplePackage {
    inst SimpleTemplate with A => B (k() -> getB,
                                     k -> x);
}
