/* This is the old ClassDeclRew.renameDefinitions(), but adapted to work on any
   TypeDecl (in particular InterfaceDecl).
*/

/* TODO TODO.
    The .methodAccess() thing appears to have been too good to be true.
    (At least it doesn't work as early as this.)
    A solution has been implemented as a replacement for this, which
    actually works -- createVirtualRenamingDeclarations and friends.
    Todo:
        - presumably the same is true for fields; incorporate fields into
          the virtual-renaming-declarations solutions and replace
          fieldAccess (this is part of a larger todo involving neglect of
          fields)
        - remove all the cruft from the old code that didn't work
        - add more tests for internal renaming (it's mind-boggling
          that this wasn't discovered until now)
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

		for (MethodDecl decl : methods.values()) {
                /* If we rename the tabstracts we have trouble recognizing
                   their signatures later. More elegant way? */
            if( decl.isTabstract() ) continue; // XXX MISSINGFEATURE?

			if (namesMap.containsKey(decl.signature())) {

				String newID = namesMap.get(decl.signature());
				newID = newID.split("\\(")[0];

                String oldSig = decl.signature();

				decl.setID(newID);
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
				}
			}
		}
    }
}
