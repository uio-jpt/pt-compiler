template T1 { // PTTemplate (PTDecl)
    class C1 { // SimpleClass
        double v1;
        C1 renameThisType;
    }
}

template T2 { // PTTemplate (PTDecl)
    class C2 { // SimpleClass
        int v2;
    }
    class MergeClass {
        int mergeMe;
        public MergeClass getMergeClass() { return this; }
    }
}

package PackageTest1 { //PTPackage (PTDecl)
    inst T1 with C1 => M; //PTInstDecl
    //           ^^^^^^^ PTDummyClass
    //           OrgID=source, ID=target
    inst T2 with C2 => M, MergeClass => M; //PTInstDecl
    class M adds {
        int addsM;
    }
 }
/*
 * med alle 3 separate klasser:
   renamed AST.ClassDecl [K]
   renamed AST.TypeAccess [, K]

   renamed AST.ConstructorDecl
   renamed AST.TypeAccess [, M]


 * getClassNamesWithDummyList
 *
 *
 */
