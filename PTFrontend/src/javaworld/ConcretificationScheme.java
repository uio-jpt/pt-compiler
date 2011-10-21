package javaworld;

import java.util.Map;

import AST.TypeDecl;
import AST.RequiredType;
import AST.TypeAccess;
import AST.Access;

public class ConcretificationScheme {
    private Map<RequiredType, TypeDecl> concretifications;

    public ConcretificationScheme( Map<RequiredType, TypeDecl> concretifications)  {
        this.concretifications = concretifications;
    }

    public ConcretificationScheme() {
        this.concretifications = new java.util.HashMap<RequiredType,TypeDecl>();
    }

    public TypeDecl getConcretification( RequiredType reqtype ) {
        return concretifications.get( reqtype );
    }

    public Map<TypeDecl, TypeAccess> createDeclToAccessMap() {
        Map<TypeDecl, TypeAccess> rv = new java.util.HashMap<TypeDecl,TypeAccess> ();
        for( RequiredType rt : concretifications.keySet() ) {
            TypeDecl targetDecl = concretifications.get( rt );
            TypeAccess targetAccess = new AST.TypeAccess( targetDecl.getID() );
            rv.put( rt, targetAccess );
        }
        for( TypeDecl x : rv.keySet() ) {
            System.out.println( "DISPLAY KEY " + x.dumpTree() + " --> " + rv.get(x).dumpTree() );
        }
        return rv;
    }
}
