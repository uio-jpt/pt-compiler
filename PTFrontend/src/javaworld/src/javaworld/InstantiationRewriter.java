package javaworld;

import AST.PTDecl;
import AST.PTPackage;

/* The point of this class is the run() method, which mutates/rewrites
   the object PTDecl in-place (it does not create a copy and modify
   that). PTDecl represents either a "template" node or a "package"
   node. */

public class InstantiationRewriter {

	public InstantiationRewriter() {
	}

	public void run(PTDecl decl) {
		PTDeclRew target = new PTDeclRew(decl);
        target.createRenamedInterfaces();
        target.createRenamedEnums();
        target.createEmptyMissingAddClasses();
		target.extendAddClassesWithInstantiatons();
		target.copyImportDecls();
		target.createInitIfPackage();
		target.flushCaches();
	}
}
