package javaworld;

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

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.ImmutableSet.Builder;

public class ClassDeclRew {
	protected final ClassDecl ext;
	private final String sourceTemplateID;

	public ClassDeclRew(ClassDecl ext, String sourceTemplateID) {
		this.sourceTemplateID = sourceTemplateID;
		Preconditions.checkArgument(sourceTemplateID != null);
		this.ext = ext;
		
	}

	/* TODO not very pretty */
	protected void renameConstructors(PTDummyClass instantiator) {
		int i = -1;
		for (BodyDecl decl : ext.getBodyDeclList()) {
			i++;
			if (decl instanceof ConstructorDecl) {
				ConstructorDecl cd = (ConstructorDecl) decl;
				ConstructorRew cdRew = new ConstructorRew(cd, sourceTemplateID);
				try {
					decl = cdRew.toMethodDecl(instantiator.getID(),
							instantiator.getOrgID());
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
		for (String possibleConflict : conflicts) {
			String tsuperName = Util.toName(templateName, className,
					possibleConflict);
			renamedVersion.put(possibleConflict, tsuperName);
		}
		renameDefinitions(renamedVersion);
	}

	protected String getSuperClassName() {
		return ext.getSuperClassName();
	}

	// TODO make pretty
	void renameDefinitions(Map<String, String> namesMap) {
		Map<String, MethodDecl> methods = ext.methodsSignatureMap();
		Map<String, SimpleSet> fields = ext.memberFieldsMap();
	
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
}
