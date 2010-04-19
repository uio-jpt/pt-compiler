template T1 { // PTTemplate (PTDecl)
    class A {
        int aLocal;
    }
    class B extends A  {
        String b;
    }
}

template T2 { // PTTemplate (PTDecl)
    class C {
        int zLocal;
    }
    class D extends C  {
        String y;
    }
}

package PackageTest1 {
    inst T1 with A => Z, B => Y;
    inst T2 with C => Z, D => Y;
    //inst T1 with A => AA, B => BB; // FIX: denne vil knekke ettersom BB ikke subklasser etter rewriting.
    class Y adds {
        int z;
    }
 }




