package javaworld;

import AST.*;

/* Utility class used by the parser. This code was embedded directly
   in pt.parser, but then duplicated, which is annoying when making
   changes. */

public class ChildClassifier {
    private AST.List<SimpleClass> simpleClassList;
    private AST.List<PTInstDecl> instDeclList;
    private AST.List<PTInterfaceDecl> intfDeclList;
    private AST.List<PTEnumDecl> enumDeclList;
    private AST.List<RequiredType> requiredTypeList;

    public AST.List<SimpleClass> getSimpleClasses() { return simpleClassList; }
    public AST.List<PTInstDecl> getInstDecls() { return instDeclList; }
    public AST.List<PTInterfaceDecl> getInterfaces() { return intfDeclList; }
    public AST.List<PTEnumDecl> getEnums() { return enumDeclList; }
    public AST.List<RequiredType> getRequiredTypes() { return requiredTypeList; }

    public ChildClassifier( AST.List<ASTNode> nodes ) {
        simpleClassList = new AST.List<SimpleClass>();
        instDeclList = new AST.List<PTInstDecl>();
        intfDeclList = new AST.List<PTInterfaceDecl>();
        enumDeclList = new AST.List<PTEnumDecl>();
        requiredTypeList = new AST.List<RequiredType>();

        for (int i=0; i<nodes.getNumChildNoTransform(); i++) {
            ASTNode n = nodes.getChildNoTransform(i);
            if (n instanceof PTInstDecl) {
                instDeclList.add( (PTInstDecl) n);
            }
            else if (n instanceof PTInterfaceDecl) {
                intfDeclList.add( (PTInterfaceDecl) n );
            }
            else if(n instanceof PTEnumDecl ) {
                enumDeclList.add( (PTEnumDecl) n );
            }
            else if(n instanceof RequiredType ) {
                requiredTypeList.add( (RequiredType) n );
            }
            else if(n instanceof SimpleClass ) {
                simpleClassList.add( (SimpleClass) n);
            } else {
                throw new IllegalStateException( "child of ptdecl could not be classified: " + n.getClass().getName() );
            }
        }
    }

}
