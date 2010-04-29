template SimpleTemplate {

    class A {
        int k;

        A getA() {
            return new A();
        }
    }

    class B {
        int j;
    }

    class SubA extends A { }
    class SubB extends B { }    
}

package AddPackage {
    inst SimpleTemplate with A => SuperM, B => SuperM, SubA => M, SubB => M;
}
