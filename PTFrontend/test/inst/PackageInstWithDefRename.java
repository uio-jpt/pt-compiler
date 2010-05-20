template SimpleTemplate {

    class A {
        int k;

        A getA() {
            return new A();
        }
    }
}

package AddPackage {
    inst SimpleTemplate with A => X (k -> newK, getA() -> getX);
}
