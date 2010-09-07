package javaworld;

import AST.Access;
import AST.Block;
import AST.List;
import AST.Modifier;
import AST.Modifiers;
import AST.Opt;
import AST.Stmt;
import AST.TemplateAncestor;
import AST.TemplateConstructor;
import AST.TypeAccess;

public class Ancestor {

	private final TemplateConstructor constructor;
	private final String sourceTemplateID;
	private final String id;

	public Ancestor(TemplateConstructor decl, String sourceTemplateID, String id) {
		this.constructor = decl;
		this.sourceTemplateID = sourceTemplateID;
		this.id = id;
	}

	public TemplateAncestor toAncestorDecl() {
		String modifiedMethodName = Util.toAncestorName(id, sourceTemplateID,
				constructor.getID());
		Modifiers m = new Modifiers(new List<Modifier>().add(new Modifier(
				"private")));
		TypeAccess t = new TypeAccess("void");
		TemplateAncestor md = new TemplateAncestor(m, t, modifiedMethodName,
				constructor.getParameterList(), new List<Access>(),
				new Opt<Block>());
		md.setBlock(new Block(new List<Stmt>()));

		for (Stmt s : constructor.getBlock().getStmtList()) {
			md.getBlock().addStmt(s);
		}
		md.IDstart = constructor.IDstart; // give the generated method the same
											// location
		// as the constructor
		// md.IDend = cd.IDend;
		return md;
	}
}
