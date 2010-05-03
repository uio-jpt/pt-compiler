template X {

    class A { int f(int x) { return 1; } }

}

template Y {
    inst X;
}

template Z {
    inst Y;
    class A adds { int h(int x) { return 4;}}
}
