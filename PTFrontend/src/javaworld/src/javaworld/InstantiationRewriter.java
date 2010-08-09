package javaworld;

import AST.PTDecl;

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
		target.makePTPackageNamesCompilable();
	}
}
