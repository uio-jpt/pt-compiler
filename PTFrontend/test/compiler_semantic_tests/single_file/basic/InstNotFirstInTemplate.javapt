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

template AddTemplate {
    class Test {
    }

    inst SimpleTemplate with A => Xylofon;

    class Xylofon adds {
        int x;
        
        Xylofon() {
        }
    }
}

package TestPackage {
    inst AddTemplate;
}
