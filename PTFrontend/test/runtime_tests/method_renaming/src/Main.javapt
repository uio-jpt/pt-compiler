template T1 {

    class A {
        int m() {
            return 42;
        }
    }
}

template T2 {
    inst T1 with A => A(m() -> f);
    
    class A adds {

        int f(int k) {
            int sum = 0;
            while (k-- > 0)
                sum += f();
            return sum;
        }
    }
}

package renamemethods {
    inst T2 with A => A(f(*) -> g);
    
    class Main {

        public static void main(String[] args) {
            A x = new A();
            System.out.println("A.g(3) = " + x.g(3));
        }
    }
}
