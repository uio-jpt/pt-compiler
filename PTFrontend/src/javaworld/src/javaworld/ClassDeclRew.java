package javaworld;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import AST.BodyDecl;
import AST.ClassDecl;
import AST.ConstructorDecl;
import AST.PTDummyClass;

public class ClassDeclRew {
	private final ClassDecl ext;
	private final Set<String> conflicts;

	public ClassDeclRew(ClassDecl ext, Set<String> conflicts) {
		this.ext = ext;
		this.conflicts = conflicts;
	}

	public void resolveConflicts(PTDummyClass instantiator) {
		HashMap<String, String> resolvedConflict = instantiator
				.getRenamedConflictsMap(conflicts);
		// System.out.println("conflicts: " + conflicts);
		// System.out.println("resolved: " + resolvedConflict);
		ext.renameDefinitions(resolvedConflict);
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

	public ClassDecl getRenamed(PTDummyClass x) {
		resolveConflicts(x);
		renameConstructors(x);
		return ext;
	}

}
