template T {

    class A { int getX() { return 1; } }
    class B { int getX() { return 2; } }
}

package P {
    inst T with A => X, B => X(getX() -> getY);

    class X adds { 
    }
}
