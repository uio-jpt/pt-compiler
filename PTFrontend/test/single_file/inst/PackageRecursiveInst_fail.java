template SimpleTemplate {
    inst SimpleTemplate; // error
    
    class A {
        int k;

        A getA() {
            return new A();
        }
    }
}
