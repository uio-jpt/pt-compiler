template T1 {
    class A {
        int twoTimer() {
            return 5;
        }
    }
    class B  {
        int twoTimer;
    }
}

package PackageTest1 {
    inst T1 with A => Z(twoTimer -> b), B => Z ;
 }




