package javaworld;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import AST.BodyDecl;
import AST.ClassDecl;
import AST.ConstructorDecl;
import AST.FieldDeclaration;
import AST.List;
import AST.MethodDecl;
import AST.PTDummyClass;
import AST.SimpleSet;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;

public class ClassDeclRew {
	private final ClassDecl ext;
	private Set<String> conflicts;

	public ClassDeclRew(ClassDecl ext) {
		this.ext = ext;
	}
	
	/* TODO not very pretty */
	public void renameConstructors(PTDummyClass instantiator) {
		int i = -1;
		for (BodyDecl decl : ext.getBodyDeclList()) {
			i++;
			if (decl instanceof ConstructorDecl) {
				ConstructorDecl cd = (ConstructorDecl) decl;
				ConstructorRew cdRew = new ConstructorRew(cd);
				try {
					decl = cdRew.toMethodDecl(instantiator.getID(),
							instantiator.getOrgID(), ext.getSuperClassName());
					ext.setBodyDecl(decl, i);
				} catch (Exception e) {
					cd.error("Could not rewrite constructor " + cd.dumpString()
							+ " to method during class merging.\n");
				}
			}
		}
	}
    
    // TODO make pretty
    void renameDefinitions(Map<String,String> namesMap) {
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

	public Set<String> getSignatures() {
		Builder<String> x = ImmutableSet.builder();
		x.addAll(ext.methodSignatures());
		x.addAll(ext.fieldNames());
		return x.build();
	}

	public List<BodyDecl> getBodyDecls() {
		return ext.getBodyDecls();
	}
    
}
