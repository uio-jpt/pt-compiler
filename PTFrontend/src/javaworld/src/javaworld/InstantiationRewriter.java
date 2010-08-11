package javaworld;

import AST.ConstructorDecl;
import AST.MethodDecl;
import AST.PTDecl;
import AST.Stmt;

public class InstantiationRewriter {

	private final PTDeclRew target;

	public InstantiationRewriter(PTDecl target) {
		this.target = new PTDeclRew(target);
	}

	public void run() {
		target.createEmptyMissingAddClasses();
		target.extendAddClassesWithInstantiatons();
		target.updateAddsSuperClasses();
		target.copyImportDecls();
		target.flushCaches();
	}
}
