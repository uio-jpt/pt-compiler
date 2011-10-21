/* This is the old ClassDeclRew.renameDefinitions(), but adapted to work on any
   TypeDecl (in particular InterfaceDecl).
*/
package javaworld;

import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;

import AST.TypeDecl;
import AST.MethodDecl;
import AST.SimpleSet;
import AST.MethodAccess;
import AST.VarAccess;
import AST.FieldDeclaration;
import AST.PTDecl;
import AST.ClassDecl;

public class DefinitionsRenamer {
    public static void renameDefinitions(TypeDecl ext, Map<String, String> namesMap) {
		Map<String, MethodDecl> methods = ext.methodsSignatureMap();
//		Map<String, MethodDecl> methods = ext.localMethodsSignatureMap();
		Map<String, SimpleSet> fields = ext.memberFieldsMap();
        
        if( namesMap.isEmpty() ) {
            return;
        }

        if( ext instanceof ClassDecl ) { // this is ugly, move it out to the caller eventually TODO
            ClassDecl cd = (ClassDecl) ext;
            if( cd.inheritsFromExtendsExternal() ) {
                cd.error( "cannot rename definitions in " + cd.getID() + " which inherits from external" );
                return;
            }
        }

        Set<String> namesToRename = new HashSet<String>();
        for( String key : namesMap.keySet() ) {
            namesToRename.add( key );
        }

        java.util.Map< MethodDecl, String > declarationsToRename = new java.util.HashMap< MethodDecl, String > ();

		for (MethodDecl decl : methods.values()) {
                /* If we rename the tabstracts we have trouble recognizing
                   their signatures later. More elegant way? */
            if( decl.isTabstract() ) continue; // XXX MISSINGFEATURE?

			if (namesMap.containsKey(decl.signature())) {
                System.out.println( "this is where everything fails: " + decl );

				String newID = namesMap.get(decl.signature());
				newID = newID.split("\\(")[0];

                declarationsToRename.put( decl, newID );
            }
        }

        System.out.println( "renaming in: " + ext );
        
        AST.ASTNode parent = ext.getParent();
        for(int i=0;i<parent.getNumChild();i++) {
            if( parent.getChild(i) == ext ) {
                System.out.println( "ext is child no " + i );
            } else {
                System.out.println( "ext IS NOT child no " + i );
            }
        }

        new MethodAccessRenamer( declarationsToRename ).mutate( ext.getParentClass( PTDecl.class ) );

		for (MethodDecl decl : methods.values()) {
                /* If we rename the tabstracts we have trouble recognizing
                   their signatures later. More elegant way? */
            if( decl.isTabstract() ) continue; // XXX MISSINGFEATURE?

			if (namesMap.containsKey(decl.signature())) {
                System.out.println( "this is where everything fails: " + decl );

				String newID = namesMap.get(decl.signature());
				newID = newID.split("\\(")[0];

				for (MethodAccess x : decl.methodAccess()) { // <-- note, very handy JaJ method (our own..)
                    System.out.println( "RENAMING AN ACCESS: " + x.getID() + " -> " + newID );
					x.setID(newID);
                }

                String oldSig = decl.signature();

				decl.setID(newID);

                if( namesToRename.contains( oldSig ) ) {
                    namesToRename.remove( oldSig );
                }
			}
		}

		for (SimpleSet simpleSet : fields.values()) {
			for (Iterator iter = simpleSet.iterator(); iter.hasNext();) {
				FieldDeclaration fieldDecl = (FieldDeclaration) iter.next();
				if (namesMap.containsKey(fieldDecl.getID())) {
					String newID = namesMap.get(fieldDecl.getID());
					for (VarAccess x : fieldDecl.fieldAccess()) { // <-- similarly, very handy JaJ method
						x.setID(newID);
                    }
                    String oldId = fieldDecl.getID();

					fieldDecl.setID(newID);

                    if( namesToRename.contains( oldId ) ) {
                        namesToRename.remove( oldId );
                    }
				}
			}
		}
    }
}
