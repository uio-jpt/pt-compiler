package javaworld;

import AST.Access;
import AST.Block;
import AST.ConstructorDecl;
import AST.ExprStmt;
import AST.List;
import AST.MethodAccess;
import AST.MethodDecl;
import AST.Opt;
import AST.ReturnStmt;
import AST.Stmt;
import AST.SuperConstructorAccess;
import AST.TemplateConstructorAccess;
import AST.TemplateConstructor;
import AST.ThisAccess;
import AST.TypeAccess;

public class ConstructorRew {

	private final ConstructorDecl cd;
	private final String sourceTemplateName;

	public ConstructorRew(ConstructorDecl cd, String sourceTemplateName) {
		this.cd = cd;
		this.sourceTemplateName = sourceTemplateName;
	}

	/*
	 * TODO Should have void returntype. cleanup!
	 */
	public MethodDecl toMethodDecl(String returnType,
			String methodName, String orgSuperClass) {
		/*
		 * Rewrite a whole constructor declaration to a method. Will also
		 * rewrite constructor invocations to method invocations based on
		 * orgSuperClass.
		 */
		// TODO TODO
		System.out.println("making " + methodName + " merged!");
		String modifiedMethodName = String.format("tsuper[%s,%s]",sourceTemplateName, methodName);
		MethodDecl md = new TemplateConstructor(cd.getModifiers(),
				new TypeAccess(returnType), modifiedMethodName,
				cd.getParameterList(), new List<Access>(), new Opt<Block>(), methodName);
		md.setBlock(new Block(new List<Stmt>()));
		if (cd.hasConstructorInvocation() && orgSuperClass != null) {
			// rewrite "super(x,y,z)" to "superA(x,y,z)" where A is the original
			// superclass
			try {
				ExprStmt s = (ExprStmt) cd.getConstructorInvocation();
				SuperConstructorAccess sa = (SuperConstructorAccess) s
						.getExpr();
				MethodAccess oldConstructorInvocationAsMethod = new MethodAccess(
						"tsuper[" + orgSuperClass + "]", sa.getArgList());
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
