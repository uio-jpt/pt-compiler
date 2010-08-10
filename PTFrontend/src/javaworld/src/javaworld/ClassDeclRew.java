package javaworld;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import AST.BodyDecl;
import AST.ClassDecl;
import AST.ConstructorDecl;
import AST.FieldDeclaration;
import AST.MethodDecl;
import AST.PTDummyClass;
import AST.SimpleSet;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import com.google.common.collect.Sets;

public class ClassDeclRew {
	private final ClassDecl ext;
	private Set<String> conflicts;

	public ClassDeclRew(ClassDecl ext) {
		this.ext = ext;
	}

	public void resolveConflicts(DummyRew instantiator) {
		Map<String, String> resolvedConflict = instantiator
				.getRenamedConflictsMap(conflicts);
		renameDefinitions(resolvedConflict);
	}

	/* TODO not very pretty */
	public void renameConstructors(PTDummyClass instantiator) {
		int i = -1;
		for (BodyDecl decl : ext.getBodyDeclList()) {
			i++;
			if (decl.isNotEmptyConstructor() && decl instanceof ConstructorDecl) {
				ConstructorDecl cd = (ConstructorDecl) decl;
				ConstructorRew cdRew = new ConstructorRew(cd);
				try {
					decl = cdRew.toMethodDecl(instantiator.getID(),
							instantiator.getOrgID(), ext.getSuperClassName());
					ext.setBodyDecl(decl, i);
					return;
				} catch (Exception e) {
					cd.error("Could not rewrite constructor " + cd.dumpString()
							+ " to method during class merging.\n");
				}
			}
		}
	}

	public ClassDecl getRenamed(DummyRew dummyr) {
		resolveConflicts(dummyr);
		renameConstructors(dummyr.instantiator);
		return ext;
	}

	/* TODO, fields and methods may collide. */
    Set<String> getDefinitionsRenamed(Map<String,String> namesMap) {
        Builder<String> definitionNames = ImmutableSet.builder();
        for (String name : ext.methodSignatures()) {
            if (namesMap.containsKey(name))
                name = namesMap.get(name);
            definitionNames.add(name);
        }
        return definitionNames.build();
    }
    
    // TODO make pretty
    public void renameDefinitions(Map<String,String> namesMap) {
        Map <String,MethodDecl>methods = ext.methodsSignatureMap();
        Map <String,SimpleSet> fields = ext.memberFieldsMap();

        for (MethodDecl decl : methods.values()) {
            if (namesMap.containsKey(decl.signature())) {
                String newID = namesMap.get(decl.signature());
                newID = newID.split("\\(")[0];
                decl.setID(newID);
            }
        }

        for (SimpleSet simpleSet : fields.values()) {
            for (Iterator iter = simpleSet.iterator(); iter.hasNext();) {
                FieldDeclaration fieldDecl = (FieldDeclaration) iter.next();
                if (namesMap.containsKey(fieldDecl.getID())) {
                    String newID = namesMap.get(fieldDecl.getID());
                    fieldDecl.setID(newID);
                }
            }
        }
    }

	public void renameTypes(HashMap<String, String> renamedClasses) {
		ext.renameTypes(renamedClasses);
	}

	public void addConflicts(Set<String> conflicts) {
		this.conflicts = conflicts;
	}
    
}
