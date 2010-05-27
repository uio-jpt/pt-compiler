
/*
 * fix: same template name, but no error detected */

template T1 { class A { int getX() { return 533; } } }
template T1 { class B { int getY() { return 533; } } }

package AddPackage {
    inst T1 with A => M;
    //inst T2 with A => M;

    class M adds {
    }
}

