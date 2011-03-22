package javaworld;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

import AST.BodyDecl;
import AST.ClassDecl;
import AST.ConstructorDecl;
import AST.FieldDeclaration;
import AST.List;
import AST.MethodAccess;
import AST.MethodDecl;
import AST.PTInstTuple;
import AST.SimpleSet;
import AST.VarAccess;
import AST.TypeAccess;
import AST.Access;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import com.google.common.collect.Maps;

class ClassDeclRew {
	protected final ClassDecl ext;
	private final String sourceTemplateID;

	ClassDeclRew(ClassDecl ext, String sourceTemplateID) {
		this.sourceTemplateID = sourceTemplateID;
		Preconditions.checkArgument(sourceTemplateID != null);
		this.ext = ext;

	}

    public HashSet<String> getImplementsList() {
        HashSet<String> rv = new HashSet<String> ();
        for( Access x : ext.getImplementsList() ) {
            TypeAccess iface = (TypeAccess) x ;
            rv.add( iface.getID() );
        }
        return rv;
    }


	/* TODO not very pretty 
	 * 
	 * Renames a constructor to a method with name minit$<<TemplateName>>$<<OriginalClassName>>
	 * Adds a super method call with the same name style if needed.
	 * 
	 */
	protected void renameConstructors(PTInstTuple instantiator) {
		int i = -1;
		for (BodyDecl decl : ext.getBodyDeclList()) {
			i++;
			if (decl instanceof ConstructorDecl) {
				ConstructorDecl cd = (ConstructorDecl) decl;
				ConstructorRew cdRew = new ConstructorRew(cd, sourceTemplateID,
						instantiator.getOrgID());
				try {
					decl = cdRew.toMethodDecl();
					ext.setBodyDecl(decl, i);

				} catch (Exception e) {
					cd.error("Could not rewrite constructor " + cd.dumpString()
							+ " to method during class merging.\n");
				}
			}
		}
	}

	protected void renameTypes(Map<String, String> renamedClasses) {
		ext.renameTypes(renamedClasses);
	}

	protected Set<String> getSignatures() {
		Builder<String> x = ImmutableSet.builder();
		x.addAll(ext.methodSignatures());
		x.addAll(ext.fieldNames());
		return x.build();
	}

	protected List<BodyDecl> getBodyDecls() {
		return ext.getBodyDecls();
	}

	protected void renameMatchingMethods(Set<String> conflicts) {
		final String templateName = sourceTemplateID;
		final String className = ext.getID();
		Map<String, String> renamedVersion = Maps.newHashMap();
		for (String memberNameToBeRenamed : conflicts) {
			String tsuperName = Util.toName(templateName, className,
					memberNameToBeRenamed);
			renamedVersion.put(memberNameToBeRenamed, tsuperName);
		}
		renameDefinitions(renamedVersion);
	}

    protected boolean isAbstract() {
        return ext.isAbstract();
    }

    protected HashSet getImplementedInterfaces() {
        return ext.implementedInterfaces();
    }

	protected String getSuperClassName() {
		return ext.getSuperClassName();
	}

	/* 
	 * Renames methods or fields which have renaming explicitly
	 * given in the inst clauses.
	 * TODO make pretty
	 */
	void renameDefinitions(Map<String, String> namesMap) {
		Map<String, MethodDecl> methods = ext.methodsSignatureMap();
		Map<String, SimpleSet> fields = ext.memberFieldsMap();

		for (MethodDecl decl : methods.values()) {
			if (namesMap.containsKey(decl.signature())) {
				String newID = namesMap.get(decl.signature());
				newID = newID.split("\\(")[0];
				for (MethodAccess x : decl.methodAccess())
					x.setID(newID);
				decl.setID(newID);
				
			}
		}

		for (SimpleSet simpleSet : fields.values()) {
			for (Iterator iter = simpleSet.iterator(); iter.hasNext();) {
				FieldDeclaration fieldDecl = (FieldDeclaration) iter.next();
				if (namesMap.containsKey(fieldDecl.getID())) {
					String newID = namesMap.get(fieldDecl.getID());
					for (VarAccess x : fieldDecl.fieldAccess())
						x.setID(newID);
					fieldDecl.setID(newID);
				}
			}
		}
	}

	@Override
	public String toString() {
		return ext.toString();
	}
}
