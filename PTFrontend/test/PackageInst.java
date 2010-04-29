template SimpleTemplate {
    
    class A {
        int k;

        A getA() {
            return new A();
        }
    }
}

package SimplePackage {
    inst SimpleTemplate;
}
