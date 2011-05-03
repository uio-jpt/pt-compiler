/* Another unrelated compiler bug means that we can't
   actually have a class extending from anything
   substantial such as Exception, so we'll use
   Object as a stand-in (because that other bug is
   not really relevant to this test).

   Imagine the test being
        template T <V extends Exception & Runnable>
        ...
        class W extends Exception implements Runnable
*/

template T <V extends Object & Runnable> {
    class A {
        V v;

        V getV() {
            return v;
        }

        public A setV(V v) {
            this.v = v;
            return this;
        }
    }
}

package P {
    inst T<W>;

    class W implements Runnable {
        public void run() {
            System.out.println( "Hello world!" );
        }
    }

    class X {
        public static void main(String args[]) {
            new A().setV(new W()).getV().run();
        }
    }
}
