package javaworld;

import AST.*;
import java.util.Set;

class ConstructorRew {

	private final ConstructorDecl cd;
	private final String templateID;
	private final String tclassID;

	ConstructorRew(ConstructorDecl cd, String templateID, String tclassID) {
		this.cd = cd;
		this.templateID = templateID;
		this.tclassID = tclassID;
	}

	protected MethodDecl toMethodDecl() {
        // This seems to preserve parameter lists, so it should ac
		String modifiedMethodName = Util.toMinitName(templateID, tclassID);
		MethodDecl md = new TemplateConstructor(cd.getModifiers(),
				new TypeAccess("void"), modifiedMethodName,
				cd.getParameterList(), new List<Access>(), new Opt<Block>(
						cd.getBlock()), tclassID, templateID);
		md.setBlock(new Block(new List<Stmt>()));
		String supername = cd.getClassDecl().getSuperClassName();

//        assert( cd instanceof PTConstructorDecl ); // TODO export this assumption to the types, file name etc
        if( cd instanceof PTConstructorDecl ) {
            PTConstructorDecl pcd = (PTConstructorDecl) cd;

/*
		if (
            supername != null
            && !cd.getClassDecl().inheritsFromExtendsExternal()
            ) {
            // If the class has an internal superclass, we add a minit call to it.
            // This is NOTBACKWARDSE.
            // In backwards E, the package (or instantiating template) should be calling us
            // (the right side of the E).

			String methodName = Util.toMinitName(templateID, supername);
			MethodAccess supercall = new MethodAccess(methodName, new List<Expr>());
            System.out.println( "adding call statement to " + templateID + ":" + supername + ": " + methodName + " = " + supercall);
			md.getBlock().addStmt(new ExprStmt(supercall));
		}
*/

            for(PTTSuperConstructorCall scc : pcd.getTSuperConstructorInvocationList() ) {
                Set<ASTNode> decls = scc.getTemplateClassIdentifier().locateTemplateClass( (PTDecl) scc.getParentClass( PTDecl.class ) );
                
                if( decls.size() == 1 ) {
                    TypeDecl decl = (TypeDecl) decls.iterator().next();
                    String tsuperClassID = decl.getID();
                    PTTemplate template = (PTTemplate) decl.getParentClass( PTTemplate.class );
                    if( template != null ) {
                        String superTemplateID = template.getID();

                        String methodName = Util.toMinitName( superTemplateID, tsuperClassID );
                        AST.List<Expr> args = scc.getArgs().fullCopy();

                        Stmt stmt =  new ExprStmt( new MethodAccess( methodName, args ) );
                        md.getBlock().addStmt( stmt );
                    }
                }
            }
        } else {
            System.out.println( "this is NOT a ptcd: " + cd  + "(shouldn't happen, temporary until cleanup)" );
        }
		
		for (Stmt s : cd.getBlock().getStmtList()) {
			md.getBlock().addStmt(s);
		}

		md.IDstart = cd.IDstart; // give the generated method the same location
									// as the constructor
		md.IDend = cd.IDend;

		return md;
	}
}