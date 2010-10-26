template T1 {

    class A {
        int a;
    }

    class B extends A {
        int b;
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
