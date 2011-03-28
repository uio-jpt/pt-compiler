template T1 {
    abstract class X {
        abstract void foo();
    }
}

template T2 {
    inst T1;
    class X adds {
        tabstract void foo();
    }
}

package P {
    inst T2;
}
