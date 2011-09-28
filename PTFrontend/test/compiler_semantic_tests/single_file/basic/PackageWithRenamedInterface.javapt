template T {
    interface TestI {
        public int add(int x, int y);
    }

    class TC implements TestI {

        public int add(int x, int y) {
            return x + y;
        }
    }
}

package P {
    inst T with TestI => TestX;

    class R {
        void testrunner() {
            TestX t = new TC();
            System.out.println(t.add(1,2));
        }
    }
}
