template T {
    class X {
        int x;

        private class Y {
            int x;
            public void set(int x_) { x = x_; }
            public int get() { return x; }
        }

        private class Z {
            int x;
            public void set(int x_) { x = x_; }
            public int get() { return x; }
            public int callRoot() {
                return new X().getFortyTwo();
            }
        };

        public int getFortyTwo() {
            return 42;
        }

        public int test() {
            int x;
            Y y = new Y();
            Z z = new Z();
            y.set( 123 );
            z.set( 456 );
            x = 42;
            this.x = 889;
            return x + this.x + y.get() + z.get() + z.callRoot();
        }
    }
}

package P {
    inst T with X => XX ( x -> foo, getFortyTwo() -> getSomething );
}
