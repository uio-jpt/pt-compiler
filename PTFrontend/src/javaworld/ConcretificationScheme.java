package javaworld;

import java.util.Map;

import AST.TypeDecl;
import AST.RequiredType;

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
}
