package javaworld;

import AST.PTDecl;
import AST.PTPackage;
import AST.ClassDecl;

/* The point of this class is the run() method, which mutates/rewrites
   the object PTDecl in-place (it does not create a copy and modify
   that). PTDecl represents either a "template" node or a "package"
   node. */

public class InstantiationRewriter {

	public InstantiationRewriter() {
	}

	public void run(PTDecl decl) { 
		
		// Går igjennom decl sine ClassDecls for å beregne hva som er
		// "virtuelle" metoder som det siste som skjer før omskrivinga starter.
		for (ClassDecl c: decl.getClassList()) {
			c.findVirtualMethods();
		}

		PTDeclRew target = new PTDeclRew(decl);
        target.debugTypeParameters();
        target.createRenamedInterfaces();
        target.createRenamedEnums();
        target.createEmptyMissingAddClasses();
		target.extendAddClassesWithInstantiatons();
		target.copyImportDecls();
		target.createInitIfPackage();
		target.flushCaches();
	}
}
