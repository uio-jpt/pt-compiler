template T1 { // PTTemplate (PTDecl)
    class X { // SimpleClass
        int x1;
        X xx1;
    }
}

template T2 { // PTTemplate (PTDecl)
    class X { // SimpleClass
        int x2;
        X xx2;
    }
}

package PackageTest1 { //PTPackage (PTDecl)
    /*
    inst T1 with C1 => M; //PTInstDecl
    //           ^^^^^^^ PTDummyClass
    //           OrgID=source, ID=target
    inst T2 with C2 => M, MergeClass => M; //PTInstDecl
    */
    inst T1 with X => Y;
    inst T2 with X => Y;
    class Y adds { 
        int fraAddsKlasse; 
    }

 }

