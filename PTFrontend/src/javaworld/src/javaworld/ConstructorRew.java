package javaworld;

import AST.*;

public class ConstructorRew {

	private final ConstructorDecl cd;
	private final String templateID;
	private final String tclassID;

	public ConstructorRew(ConstructorDecl cd, String templateID, String tclassID) {
		this.cd = cd;
		this.templateID = templateID;
		this.tclassID = tclassID;
	}

	/*
	 * TODO cleanup!
	 */
	protected MethodDecl toMethodDecl() {
		/*
		 * Rewrite a whole constructor declaration to a method.
		 */
		// TODO TODO
		String modifiedMethodName = Util.toMinitName(templateID, tclassID);
		MethodDecl md = new TemplateConstructor(cd.getModifiers(),
				new TypeAccess("void"), modifiedMethodName,
				cd.getParameterList(), new List<Access>(), new Opt<Block>(
						cd.getBlock()), tclassID, templateID);
		md.setBlock(new Block(new List<Stmt>()));
		String supername = cd.getClassDecl().getSuperClassName();
		if (supername != null) {
			String methodName = Util.toMinitName(templateID, supername);
			MethodAccess supercall = new MethodAccess(methodName, new List<Expr>());
			md.getBlock().addStmt(new ExprStmt(supercall));
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
