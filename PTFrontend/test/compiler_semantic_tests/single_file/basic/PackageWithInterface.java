template T {
    interface TestI {
        int add(int x, int y);
    }

    class TC implements TestI {

        int add(int x, int y) {
            return x + y;
        }
    }
}

package P {

    class R {
        void testrunner() {
            TestI t = new TC();
            System.out.println(t.add(1,2));
        }
    }
}
