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

        System.out.println( "test BEFORE: " + decl );
        for( ClassDecl cd : decl.getClassList() ) {
            System.out.println( "test-class: " + cd );
            Map<String,MethodDecl> map = cd.methodsSignatureMap();
            for( String key : map.keySet() ) {
                System.out.println( key + " -- " + map.get( key ) + " -- " + map.get(key).getClass().getName() + "-- abstract? " + map.get(key).isAbstract());
            }

            System.out.println( "unimplemented methods: " + cd.unimplementedMethods() );
        }
        for( InterfaceDecl cd : decl.getPTInterfaceDeclList() ) {
            System.out.println( "test-interface: " + cd );
            Map<String,MethodDecl> map = cd.methodsSignatureMap();
            for( String key : map.keySet() ) {
                System.out.println( key + " -- " + map.get( key ) + " -- " + map.get(key).getClass().getName() + "-- abstract? " + map.get(key).isAbstract() + " -- " + map.get( key ).getPTEarlySignature() );
            }
        }
        decl.flushCaches();

		PTDeclRew target = new PTDeclRew(decl);

        target.getParameterRewriter();
        // this creates the mappings at a well-defined time

        target.createRenamedEnums();
        target.createEmptyMissingAddClasses();

        target.createMergedInterfaces();

		target.extendAddClassesWithInstantiatons();
		target.copyImportDecls();
		target.createInitIfPackage();

//        target.removeInstStatements();
		target.flushCaches();

        System.out.println( "test AFTER: " + decl );
        for( ClassDecl cd : decl.getClassList() ) {
            System.out.println( "test-class: " + cd );
            Map<String,MethodDecl> map = cd.methodsSignatureMap();
            for( String key : map.keySet() ) {
                System.out.println( key + " -- " + map.get( key ) + " -- " + map.get(key).getClass().getName() + "-- abstract? " + map.get(key).isAbstract());
            }

            System.out.println( "unimplemented methods: " + cd.unimplementedMethods() );
        }
        for( InterfaceDecl cd : decl.getPTInterfaceDeclList() ) {
            System.out.println( "test-interface: " + cd );
            Map<String,MethodDecl> map = cd.methodsSignatureMap();
            for( String key : map.keySet() ) {
                System.out.println( key + " -- " + map.get( key ) + " -- " + map.get(key).getClass().getName() + "-- abstract? " + map.get(key).isAbstract());
            }
        }
        decl.flushCaches();
	}
}
