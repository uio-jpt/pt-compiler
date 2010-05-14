template SimpleTemplate {
    
    class A {
        int k;

        A getA(int x) {
            return new A();
        }
    }
}

package SimplePackage {
    inst SimpleTemplate with A => B (getA(int) -> getB);
}
