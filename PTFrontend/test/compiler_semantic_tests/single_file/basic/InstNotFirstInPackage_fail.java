/* Not sure whether this should really be a _fail test, but
   in the interest of maintaining the status quo I'm making
   it one until I hear something to the contrary -svk */

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
    class Test {
    }

    inst SimpleTemplate with A => Xylofon;

    class Xylofon adds {
        int x;
        
        Xylofon() {
        }
    }
}
