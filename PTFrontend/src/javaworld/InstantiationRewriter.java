package javaworld;

import AST.PTDecl;
import AST.PTPackage;
import AST.ClassDecl;
import AST.InterfaceDecl;
import AST.MethodDecl;

import java.util.Map;

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

        // could we use clone and then _mutate_ the objects we're
        // _copying_ from, to subject them to extensive treatment, e.g.
        // parametrization?

        decl.flushCaches();

		PTDeclRew target = new PTDeclRew(decl);

        System.out.println( "GOING TO REWRITE: " + decl.getID() );
        System.out.println( "GOING TO REWRITE: " + decl.dumpTree() );

        target.getParameterRewriter();
        // this creates the mappings at a well-defined time

        target.createRenamedEnums();
        target.createEmptyMissingAddClasses();
        
        System.out.println( "DID REWRITE # 1: " + decl.dumpTree() );

        target.createMergedInterfaces();
        System.out.println( "DID REWRITE # 2: " + decl.dumpTree() );
        target.createMergedRequiredTypes();
        System.out.println( "DID REWRITE # 3: " + decl.dumpTree() );

		target.extendAddClassesWithInstantiatons();
        System.out.println( "DID REWRITE # 4: " + decl.dumpTree() );

        target.updateAccessesToInternalRenames();
        System.out.println( "DID REWRITE # 5: " + decl.dumpTree() );

		target.copyImportDecls();
		target.createInitIfPackage();

        target.concretifyRequiredTypes();
        System.out.println( "DID REWRITE # 6: " + decl.dumpTree() );

//        target.removeInstStatements();
		target.flushCaches();

        decl.checkRedundantExtends();

        decl.flushCaches();

        System.out.println( "all done: " + decl.dumpTree() );
        System.out.println( "all done: " + decl );
        System.out.println( "all done[2]: " + decl.dumpTree() );
	}
}
