package javaworld;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import AST.BodyDecl;
import AST.ClassDecl;
import AST.PTDummyClass;
import AST.SimpleClass;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

public class SimpleClassRew {
	private final SimpleClass decl;
	private Collection<PTDummyClass> dummies;
	private Set<String> conflicts;

	public SimpleClassRew(SimpleClass decl, Multimap<String, PTDummyClass> nameAndDummies) {
		this.decl = decl;
		checkIfSane(nameAndDummies); 
		dummies = nameAndDummies.get(decl.getID());
		conflicts = getConflicts();
	}

	public boolean mergingIsPossible() {
		return addsResolvesConflict() && addsHasOwnConstructor();
	}

	public void attemptMerging() {
		Collection<ClassDecl> instantiators = copyAndRenameForMerging();

		if (dummies.size() > 1)
			decl.hasMulitpleInstantiators = true; // TODO check where this is read

		if (mergingIsPossible()) {
			for (ClassDecl instantiator : instantiators)
				addDecls(instantiator);
		}
	}

	public void addDecls(ClassDecl source) {
		ClassDecl target = decl.getClassDecl();
		for (BodyDecl bodyDecl : source.getBodyDecls()) {
			if (bodyDecl.isNotEmptyConstructor())
				target.addBodyDecl(bodyDecl);
		}
	}

	public boolean addsResolvesConflict() {
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

	// TOOD recheck correctness ...
	public Set<String> getConflicts() {
		Set<String> collisions = Sets.newHashSet();
		Set<String> allDefinitions = decl.getClassDecl().methodSignatures();

		for (PTDummyClass dummy : dummies) {
			DummyRew x = new DummyRew(dummy);
			Set<String> instanceDecls = x.getDefinitionsRenamed();
			Set<String> localCollisions = Sets.intersection(instanceDecls, allDefinitions);
			allDefinitions.addAll(instanceDecls);
			collisions.addAll(localCollisions);
		}
		return collisions;
	}

	// TODO tautology?
	public boolean addsHasOwnConstructor() {
		return decl.getClassDecl().getConstructorDeclList().size() > 0;
	}

	public Collection<ClassDecl> copyAndRenameForMerging() {
		Collection<ClassDecl> copiesToBeMerged = Lists.newLinkedList();
		for (PTDummyClass x : dummies) {
			DummyRew dummyr = new DummyRew(x);
			ClassDeclRew ext = dummyr.getRenamedSourceClass();
			ext.addConflicts(conflicts);
			copiesToBeMerged.add(ext.getRenamed(dummyr));
		}
		return copiesToBeMerged;
	}


	public void checkIfSane(Multimap<String, PTDummyClass> nameAndDummies) {
		if (nameAndDummies.containsKey(decl.getID())) {
			if (!decl.isAddsClass()) {
				decl.error("Class "
						+ decl.getID()
						+ " has dual roles. It's both defined as an inpedendent class and as a template class instantiation.\n");
			}
		} else if (decl.isAddsClass()) {
			decl.error(decl.getID()
					+ " is an add class, template source class not found!\n");
		}
	}
}
