template SimpleTemplate {
    
    class A {
        A getA(int x) {
            return new A();
        }
        
        A getA() {
            return new A();
        }
   }

    class B {
        B getA() {
            return new B();
        }
        
        B getA(int x) {
            return new B();
        }
    }
    
}

package SimplePackage {
    inst SimpleTemplate with A => Z (getA(*) -> getZ),
        B => Z;

}
