template T1 {
    class C1 {
        double v1;
    }
}

template T2 {
    class C2 {
        int v2;
    }
}

package PackageTest1 {
    inst T1 with C1 => M;
    inst T2 with C2 => q;
 }
