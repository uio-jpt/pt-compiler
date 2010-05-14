template SimpleTemplate {
    
    class A {
        int k;

        A getA(int k) {
            return new A();
        }
    }
}

package SimplePackage {
    inst SimpleTemplate with A => A (getA(double) -> getB);
}
