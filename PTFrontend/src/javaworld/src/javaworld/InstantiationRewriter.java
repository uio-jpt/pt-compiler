package javaworld;

import AST.PTDecl;
import AST.PTPackage;

public class InstantiationRewriter {

	public InstantiationRewriter() {
	}

	public void run(PTDecl decl) {
		PTDeclRew target = new PTDeclRew(decl);
		target.createEmptyMissingAddClasses();
		target.extendAddClassesWithInstantiatons();
		target.copyImportDecls();
		target.createInitIfPackage();
		target.flushCaches();
	}
}
