package javaworld;

import AST.*;

public class ConstructorRew {

	private final ConstructorDecl cd;
	private final String sourceTemplateName;

	public ConstructorRew(ConstructorDecl cd, String sourceTemplateName) {
		this.cd = cd;
		this.sourceTemplateName = sourceTemplateName;
	}

	/*
	 * TODO cleanup!
	 */
	protected MethodDecl toMethodDecl(String returnType,
			String tclassID, String templateID) {
		/*
		 * Rewrite a whole constructor declaration to a method. Will also
		 * rewrite constructor invocations to method invocations based on
		 * orgSuperClass.
		 */
		// TODO TODO
		String modifiedMethodName = Util.toName(templateID, tclassID);
		MethodDecl md = new TemplateConstructor(cd.getModifiers(),
				new TypeAccess(returnType), modifiedMethodName,
				cd.getParameterList(), new List<Access>(), new Opt<Block>(), tclassID);
		md.setBlock(new Block(new List<Stmt>()));
		if (cd.hasConstructorInvocation() && templateID != null) {
			// rewrite "super(x,y,z)" to "superA(x,y,z)" where A is the original
			// superclass
			Util.print("inside if block");
			try {
				ExprStmt s = (ExprStmt) cd.getConstructorInvocation();
				SuperConstructorAccess sa = (SuperConstructorAccess) s
						.getExpr();
				MethodAccess oldConstructorInvocationAsMethod = new MethodAccess(
						modifiedMethodName, sa.getArgList());
				oldConstructorInvocationAsMethod.IDstart = sa.IDstart;
				oldConstructorInvocationAsMethod.IDend = sa.IDend;
				md.getBlock().addStmt(
						new ExprStmt(oldConstructorInvocationAsMethod));
			} catch (Exception e) {
				cd.getConstructorInvocation().error(
						"Could not rewrite constructor invocation to method: "
								+ e + "\n");
			}
		}
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
