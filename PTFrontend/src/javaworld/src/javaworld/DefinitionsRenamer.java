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
            System.out.println( "expecting to rename: " + key );
            namesToRename.add( key );
        }
        System.out.println( "expecting in this: " + ext );
        System.out.println( "LIST BEGIN" );
        for( MethodDecl decl : methods.values() ) {
            System.out.println( "LIST ELEMENT " + decl.signature() + "/" + decl.getPTEarlySignature() );
        }
        System.out.println( "LIST END" );

		for (MethodDecl decl : methods.values()) {
                /* If we rename the tabstracts we have trouble recognizing
                   their signatures later. More elegant way? */
            if( decl.isTabstract() ) continue; // XXX MISSINGFEATURE?
            
            System.out.println( "LOOKING FOR " + decl.signature() );
            System.out.println( "LOOKING FOR EARLYSIG " + decl.getPTEarlySignature() );

			if (namesMap.containsKey(decl.signature())) {
				String newID = namesMap.get(decl.signature());
                System.out.println( "new id: " + newID );
				newID = newID.split("\\(")[0];
                System.out.println( "new id': " + newID );
                System.out.println( "problem with decl? " + decl + " " + decl.getClass().getName() );

				for (MethodAccess x : decl.methodAccess()) { // <-- note, very handy JaJ method
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
                    System.out.println( "renamed: " + oldId + " -> " + newID );

                    if( namesToRename.contains( oldId ) ) {
                        namesToRename.remove( oldId );
                    }
				}
			}
		}
    }
}
