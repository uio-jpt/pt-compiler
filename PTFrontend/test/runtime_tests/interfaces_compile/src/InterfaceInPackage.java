package P {
    /* Looks silly and has nothing to do with templates, but it
       used to fail. -svk */

    interface TestI {
        public int add(int x, int y);
    }

    class TC implements TestI {

        public int add(int x, int y) {
            return x + y;
        }
    }

    class R {
        void testrunner() {
            TestI t = new TC();
            System.out.println(t.add(1,2));
        }

        public static void main(String args[]) {
            new R().testrunner();
        }
    }

}
