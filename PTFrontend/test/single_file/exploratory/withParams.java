template T1 {

    class A {
        int a;

        A() {
            this.a = 42;
        }
    }

    class B extends A {

        int b;

        B() {
            super();
            this.b = 42;
        }
    }
}

package P {
    inst T1 with A => X, B => Y;

    class X adds {

        int x;

        X(int a, int x) {
            this.x = x;
            System.out.println(x);
        }
    }

    class Y adds {
        int y;

        Y(int a, int b, int y) {
            super(1,2);
            this.y = y;
        }
    }
}
