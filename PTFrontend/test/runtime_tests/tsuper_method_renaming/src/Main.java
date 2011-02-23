template T1 {

    class A {

        int m(int k) {
            return k;
        }
    }
}

template T2 {
    inst T1;
    
    class A adds {

        int m(int k) {
            return tsuper[A].m(k) + k + 1;
        }
    }
}

package tsupertest {
    inst T2;

    class A adds {
        int m(int k) {
            return tsuper[A].m(k) + k + 2;
        }
    }

    
    class Main {

        public static void main(String[] args) {
            A x = new A();
            System.out.println("A.m(3) = " + x.m(3));
        }
    }
}
