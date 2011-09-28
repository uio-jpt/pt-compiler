template T {
    public class X {
        public void f(int x) {
        }

    }

    public class XX extends X {
        public void f(int x) {
        }
    }
}

package P {
    inst T with X => Y ( f(*) -> g ), XX => YY ( f(int) -> h );
}
