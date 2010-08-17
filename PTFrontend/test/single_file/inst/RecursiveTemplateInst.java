template X {

    class X { int f(int x) { return 1; } }

}

template Y {
    inst X with X => Y;
}

template Z {
    inst Y with Y => Z;
    class Z adds { int h(int x) { return 4;}}
}

package A {
    inst Z with Z => A;

    class A adds {
        int h(int x) {
            return tsuper[Z].h(0) + 3;
        }
    }
}
