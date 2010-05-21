template SimpleTemplate {

    class A {
        int k;

        A getA() {
            return new A();
        }
    }
}

package AddPackage {
    inst SimpleTemplate;

    class A adds {
        int x;
    }
    
}
