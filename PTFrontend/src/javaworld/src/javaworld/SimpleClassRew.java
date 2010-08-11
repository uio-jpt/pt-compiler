package javaworld;

import java.util.Collection;
import java.util.Set;

import AST.BodyDecl;
import AST.ClassDecl;
import AST.List;
import AST.PTDummyClass;
import AST.SimpleClass;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

public class SimpleClassRew {
	private final SimpleClass decl;
	private Collection<PTDummyClass> dummies;
	private Set<String> conflicts;
	private Collection<ClassDeclRew> renamedSources;
	
	public SimpleClassRew(SimpleClass decl, Multimap<String, PTDummyClass> nameAndDummies) {
		this.decl = decl;
		checkIfSane(nameAndDummies); 
		dummies = nameAndDummies.get(decl.getID());
		renamedSources = getRenamedAgnosticInstClasses();
		conflicts = getConflicts();
	}

	public void extendClass() {
		renameResolvedConflicts();

		if (mergingIsPossible()) {
			for (ClassDeclRew source : renamedSources) {
				addDecls(source.getBodyDecls());
			}
		}
	}

	private boolean addsResolvesConflict() {
		Set<String> addRefinements = decl.getClassDecl().methodSignatures();
		for (String conflictingName : conflicts) {
			if (!addRefinements.contains(conflictingName)) {
				decl.error(conflictingName
						+ " is an unresolved conflict during merging.\n");
				return false;
			}
		}
		return true;
	}

	/** Had a bug with views, switched to immutableSets. Code may be written more concise.
	 * Returns intersection of all renamed signatures (fields and methods) from all tsuperclasses.
	 * 
	 * (since getConflicts needs all classes renamed. maybe it should use a renamed copy for these purposes.)
	 */
	private Set<String> getConflicts() {
		Set<String> collisions = ImmutableSet.of();
		Set<String> allDefinitions = ImmutableSet.copyOf((decl.getClassDecl().methodSignatures()));
		for (ClassDeclRew decl : renamedSources) {
			Set<String> instanceDecls = decl.getSignatures();
			Set<String> localCollisions = Sets.intersection(instanceDecls, allDefinitions);
			allDefinitions = ImmutableSet.copyOf(Sets.union(allDefinitions, instanceDecls));
			collisions = ImmutableSet.copyOf(Sets.union(collisions, localCollisions));
		}
		return collisions;
	}

	private void renameResolvedConflicts() {
		for (ClassDeclRew decl : renamedSources) 
			decl.renameMatchingMethods(conflicts);
	}


	// TODO tautology?
	private boolean addsHasOwnConstructor() {
		return decl.getClassDecl().getConstructorDeclList().size() > 0;
	}

	private boolean mergingIsPossible() {
		return addsResolvesConflict() && addsHasOwnConstructor();
	}

	private void addDecls(List<BodyDecl> bodyDecls) {
		ClassDecl target = decl.getClassDecl();
		for (BodyDecl bodyDecl : bodyDecls) {
			target.addBodyDecl(bodyDecl);
		}
	}

	/**
	 * Renamed classes are not cross checked. 
	 * If there's a method name conflict that the adds class resolves,
	 * then the correct renaming of the conflicting classes will be performed later on.
	 * @return all classes that will be merge into current. 
	 */
	private Collection<ClassDeclRew> getRenamedAgnosticInstClasses() {
		Collection<ClassDeclRew> instClasses = Lists.newLinkedList();
		for (PTDummyClass x : dummies) {
			DummyRew dummyr = new DummyRew(x);
			ClassDeclRew ext = dummyr.getRenamedSourceClass();
			ext.setSourceTemplateName(dummyr.getSourceTemplateName());
			instClasses.add(ext);
		}
		return instClasses;
	}

	private void checkIfSane(Multimap<String, PTDummyClass> nameAndDummies) {
		if (nameAndDummies.containsKey(decl.getID())) {
			if (!decl.isAddsClass()) {
				decl.error("Class "
						+ decl.getID()
						+ " has dual roles. It's both defined as an inpedendent class and as a template adds class.\n");
			}
		} else if (decl.isAddsClass()) {
			decl.error(decl.getID()
					+ " is an add class, template source class not found!\n");
		}
	}
	
	
}
