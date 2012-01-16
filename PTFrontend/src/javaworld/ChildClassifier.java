package javaworld;

import AST.*;

/* Utility class used by the parser. This code was embedded directly
   in pt.parser, but then duplicated, which is annoying when making
   changes. */

public class ChildClassifier {
    private AST.List<SimpleClass> simpleClassList;
    private AST.List<PTInstDecl> instDeclList;
    private AST.List<InterfaceDecl> intfDeclList;
    private AST.List<PTEnumDecl> enumDeclList;
    private AST.List<RequiredType> requiredTypeList;
    private AST.List<RequiredType> requiredTypeAdditionList;

    public AST.List<SimpleClass> getSimpleClasses() { return simpleClassList; }
    public AST.List<PTInstDecl> getInstDecls() { return instDeclList; }
    public AST.List<InterfaceDecl> getInterfaces() { return intfDeclList; }
    public AST.List<PTEnumDecl> getEnums() { return enumDeclList; }
    public AST.List<RequiredType> getRequiredTypes() { return requiredTypeList; }
    public AST.List<RequiredType> getRequiredTypeAdditions() { return requiredTypeAdditionList; }

    public ChildClassifier( AST.List<ASTNode> nodes ) {
        simpleClassList = new AST.List<SimpleClass>();
        instDeclList = new AST.List<PTInstDecl>();
        intfDeclList = new AST.List<InterfaceDecl>();
        enumDeclList = new AST.List<PTEnumDecl>();
        requiredTypeList = new AST.List<RequiredType>();
        requiredTypeAdditionList = new AST.List<RequiredType>();

        for (int i=0; i<nodes.getNumChildNoTransform(); i++) {
            ASTNode n = nodes.getChildNoTransform(i);
            if (n instanceof PTInstDecl) {
                instDeclList.add( (PTInstDecl) n);
            }
            else if (n instanceof InterfaceDecl) {
                intfDeclList.add( (InterfaceDecl) n );
            }
            else if(n instanceof PTEnumDecl ) {
                enumDeclList.add( (PTEnumDecl) n );
            }
            else if(n instanceof RequiredType ) {
                if( n.isRequiredAdds() ) {
                    requiredTypeAdditionList.add( (RequiredType) n );
                } else {
                    requiredTypeList.add( (RequiredType) n );
                }
            }
            else if(n instanceof SimpleClass ) {
                simpleClassList.add( (SimpleClass) n);
            } else {
                throw new IllegalStateException( "child of ptdecl could not be classified: " + n.getClass().getName() );
            }
        }
    }

}
