package javaworld;

import java.util.Map;

import AST.TypeDecl;
import AST.RequiredType;
import AST.TypeAccess;
import AST.Access;
import AST.PTDecl;
import AST.ASTNode;

import com.google.common.base.Joiner;

public class ConcretificationScheme {
    private Map<RequiredType, TypeDecl> concretifications;
    private ASTNode context;

    public ConcretificationScheme( Map<RequiredType, TypeDecl> concretifications, ASTNode context)  {
        this.concretifications = concretifications;
        this.context = context;
    }

    public String toString() {
        java.util.List<String> l = new java.util.ArrayList();
        for( RequiredType rt : concretifications.keySet() ) {
            l.add( rt.getID() + " <= " + concretifications.get( rt ).getID() );
        }
        return "Concretifications(" + Joiner.on( "; " ).join( l ) + ")";
    }

    public ConcretificationScheme(ASTNode node) {
        this.concretifications = new java.util.HashMap<RequiredType,TypeDecl> ();
        this.context = node;
    }

    public ConcretificationScheme() {
        this.concretifications = new java.util.HashMap<RequiredType,TypeDecl>();
        System.out.println(" [warning] making ConcretificationScheme with null context" );
    }

    public TypeDecl getConcretification( RequiredType reqtype ) {
        return concretifications.get( reqtype );
    }

    public ASTNode getContext() {
        return context;
    }

    public Map<TypeDecl, TypeAccess> createDeclToAccessMap() {
        Map<TypeDecl, TypeAccess> rv = new java.util.HashMap<TypeDecl,TypeAccess> ();
        for( RequiredType rt : concretifications.keySet() ) {
            TypeDecl targetDecl = concretifications.get( rt );
            TypeAccess targetAccess = new AST.TypeAccess( targetDecl.getID() );
            rv.put( rt, targetAccess );
        }
        return rv;
    }
}
