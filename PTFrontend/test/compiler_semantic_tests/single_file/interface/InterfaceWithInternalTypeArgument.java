template T {
    class A {
    }

    interface B {
        public void f(A x);
    }
}

package P {
    inst T with B => Y ( f(A) -> g ), A => A;
}
