template T1 {

    class A {
        int x;
        
        int m(int k) {
            return k * x;
        }

        A() {
            x = 5;
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
        int z;
        
        int m(int k) {
            return tsuper[A].m(k) + k + z;
        }

        A(int z) {
            this.z = z;
        }
    }

    
    class Main {

        public static void main(String[] args) {
            A x = new A(20);
            System.out.println("A.m(3) = " + x.m(3));
        }
    }
}
