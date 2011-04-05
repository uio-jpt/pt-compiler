package javaworld;

import AST.*;

/* Utility class used by the parser. This code was embedded directly
   in pt.parser, but then duplicated, which is annoying when making
   changes. */

public class ChildClassifier {
    private AST.List<SimpleClass> simpleClassList;
    private AST.List<PTInstDecl> instDeclList;
    private AST.List<PTInterfaceDecl> intfDeclList;

    public AST.List<SimpleClass> getSimpleClasses() { return simpleClassList; }
    public AST.List<PTInstDecl> getInstDecls() { return instDeclList; }
    public AST.List<PTInterfaceDecl> getInterfaces() { return intfDeclList; }

    public ChildClassifier( AST.List<ASTNode> nodes ) {
        simpleClassList = new AST.List<SimpleClass>();
        instDeclList = new AST.List<PTInstDecl>();
        intfDeclList = new AST.List<PTInterfaceDecl>();

        for (int i=0; i<nodes.getNumChildNoTransform(); i++) {
            ASTNode n = nodes.getChildNoTransform(i);
            if (n instanceof PTInstDecl) {
                instDeclList.add( (PTInstDecl) n);
            }
            else if (n instanceof PTInterfaceDecl) {
                intfDeclList.add( (PTInterfaceDecl) n );
            }
            else if(n instanceof PTEnumDecl ) {
                PTEnumDecl ed = (PTEnumDecl) n;
                System.out.println( "warning: ignoring enum \"" + ed.getID() + "\"" );
            }
            else if(n instanceof SimpleClass ) {
                simpleClassList.add( (SimpleClass) n);
            } else {
                throw new IllegalStateException( "child of ptdecl could not be classified: " + n.getClass().getName() );
            }
        }
    }

}
