template SimpleTemplate {

    class A {
        int k;

        A getA() {
            return new A();
        }

        A() {
            k = 123;
        }
    }

}

package AddPackage {
    inst SimpleTemplate with A => Xylofon;

    class Xylofon adds {
        int x;
        
        Xylofon() {
        }
    }
}
