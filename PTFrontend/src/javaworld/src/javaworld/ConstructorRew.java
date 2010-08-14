package javaworld;

import AST.*;

public class ConstructorRew {

	private final ConstructorDecl cd;
	private final String templateID;

	public ConstructorRew(ConstructorDecl cd, String templateID) {
		this.cd = cd;
		this.templateID = templateID;
	}

	/*
	 * TODO cleanup!
	 */
	protected MethodDecl toMethodDecl(String returnType,
			String tclassID) {
		/*
		 * Rewrite a whole constructor declaration to a method. Will also
		 * rewrite constructor invocations to method invocations based on
		 * orgSuperClass.
		 */
		// TODO TODO
		String modifiedMethodName = Util.toName(templateID, tclassID);
		MethodDecl md = new TemplateConstructor(cd.getModifiers(),
				new TypeAccess(returnType), modifiedMethodName,
				cd.getParameterList(), new List<Access>(), new Opt<Block>(), tclassID, templateID);
		md.setBlock(new Block(new List<Stmt>()));

		for (Stmt s : cd.getBlock().getStmtList()) {
			md.getBlock().addStmt(s);
		}
		md.getBlock().addStmt(new ReturnStmt(new ThisAccess()));
		md.IDstart = cd.IDstart; // give the generated method the same location
									// as the constructor
		md.IDend = cd.IDend;
		return md;
	}
}
