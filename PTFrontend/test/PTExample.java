template T1 { // PTTemplate (PTDecl)
    class A { // SimpleClass
        void f () { }
    }
}

template T2 { // PTTemplate (PTDecl)
    class B { // SimpleClass
        void f () { }
    }
}

package PackageTest1 { //PTPackage (PTDecl)
    /*
    inst T1 with C1 => M; //PTInstDecl
    //           ^^^^^^^ PTDummyClass
    //           OrgID=source, ID=target
    inst T2 with C2 => M, MergeClass => M; //PTInstDecl
    */
    inst T1 with A => Y;
    inst T2 with B => Y;
    class Y adds { 
        int fraAddsKlasse; 

        // goal 1
        // baade A og B har f.. => feilmelding!

        // goal 2
        // dette parserer OK:
        Y() {
            super[A]();
        }

        void f() {
            super[A].f();
            super[B].f();
        }

        // goal 3
        // fiks renaming ala A => X (k -> y)
    }

 }

