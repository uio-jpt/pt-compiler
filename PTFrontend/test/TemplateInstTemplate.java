template T1 {
    class A {
        A(String s) { }
        int f() { return 3; }
        int g() { return 3; }
    }
}

template T2 {
    inst T1;
}
