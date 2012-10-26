package javaworld;

import AST.*;
import java.util.Set;

class ConstructorRew {

	private final ConstructorDecl cd;
	private final String templateID;
	private final String tclassID;
    private final PTInstDecl instantiation;

	ConstructorRew(ConstructorDecl cd, String templateID, String tclassID, PTInstDecl instantiation) {
		this.cd = cd;
		this.templateID = templateID;
		this.tclassID = tclassID;
        this.instantiation = instantiation;
	}

	protected MethodDecl toMethodDecl() {
        // This seems to preserve parameter lists, so it should ac
//		String modifiedMethodName = Util.toMinitName( templateID, tclassID);
		String modifiedMethodName = Util.toUniqueMinitName( instantiation, tclassID);
		MethodDecl md = new TemplateConstructor(cd.getModifiers(),
				new TypeAccess("void"), modifiedMethodName,
				cd.getParameterList(), new List<Access>(), new Opt<Block>(
						cd.getBlock()), tclassID, templateID);
		md.setBlock(new Block(new List<Stmt>()));
		String supername = cd.getClassDecl().getSuperClassName();

//        assert( cd instanceof PTConstructorDecl ); // TODO export this assumption to the types, file name etc

        // there is code duplication with simpleclassrew here, annoying, todo

        if( cd instanceof PTConstructorDecl ) {
            PTConstructorDecl pcd = (PTConstructorDecl) cd;

            for(PTTSuperConstructorCall scc : pcd.getTSuperConstructorInvocationList() ) {
                PTDecl contextOfAccess = (PTDecl) scc.getParentClass( PTDecl.class );
                Set<ASTNode> decls = scc.getTemplateClassIdentifier().locateTemplateClass( contextOfAccess );
                Set<PTInstDecl> superInstantiations = scc.getTemplateClassIdentifier().locateInstantiation( contextOfAccess );

                if( superInstantiations.size() > 1 ) {
                    scc.error( "ambiguous reference to instantiation" );
                    continue;
                }
                if( superInstantiations.size() != 1 ) {
                    scc.error( "reference to unknown instantiation rewriting constructor" );
                    System.out.println( "SOUGHT THIS, FOUND NOTHING " + scc.getTemplateClassIdentifier() );
                    continue;
                }

                PTInstDecl superInstantiation = superInstantiations.iterator().next();

                if( decls.size() == 1 ) {
                    TypeDecl decl = (TypeDecl) decls.iterator().next();
                    String tsuperClassID = decl.getID();
                    PTTemplate template = (PTTemplate) decl.getParentClass( PTTemplate.class );
                    if( template != null ) {
                        String superTemplateID = template.getID();

                        String methodName = Util.toUniqueMinitName( superInstantiation, tsuperClassID );
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
