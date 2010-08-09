package javaworld;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import AST.BodyDecl;
import AST.ClassDecl;
import AST.CompilationUnit;
import AST.ImportDecl;
import AST.List;
import AST.Modifiers;
import AST.Opt;
import AST.PTClassAddsDecl;
import AST.PTDecl;
import AST.PTDummyClass;
import AST.PTInstDecl;
import AST.PTTemplate;
import AST.SimpleClass;

public class InstantiationRewriter {

	private final PTDecl target;

	public InstantiationRewriter(PTDecl target) {
		this.target = target;
	}

	public void run() {
		createEmptyMissingAddClasses();
		extendAddClassesWithInstantiatons();
		updateAddsSuperClasses();
		copyImportDecls();
		target.flushCaches();
		target.makePTPackageNamesCompilable();
	}

	public void createEmptyMissingAddClasses() {
		HashSet<String> addClasses = target.getAdditionClassNamesSet();
		HashMap<String, LinkedList<PTDummyClass>> nameAndDummies = getClassNamesWithDummyList();
		Set<String> missingAddsClass = new HashSet<String>(
				nameAndDummies.keySet());
		missingAddsClass.removeAll(addClasses);

		for (String name : missingAddsClass) {
			ClassDecl cls = new ClassDecl(new Modifiers(), name, new Opt(),
					new List(), new List<BodyDecl>());
			PTClassAddsDecl addClass = new PTClassAddsDecl(cls);
			target.addSimpleClass(addClass);
		}
	}

	public void extendAddClassesWithInstantiatons() {
		HashMap<String, LinkedList<PTDummyClass>> nameAndDummies = getClassNamesWithDummyList();
		for (SimpleClass decl : target.getSimpleClassList()) {
			if (decl.isExtensible(nameAndDummies))
				decl.initiateExtension(nameAndDummies);
		}
	}

	public void updateAddsSuperClasses() {
		HashMap<String, LinkedList<PTDummyClass>> namesToDummyList = getClassNamesWithDummyList();
		for (PTClassAddsDecl decl : target.getAdditionClassList()) {
			if (namesToDummyList.containsKey(decl.getID()))
				// error check elsewhere
				decl.updateSuperName(namesToDummyList.get(decl.getID()));
		}
	}

	public void copyImportDecls() {
		CompilationUnit ownCU = target.getCompilationUnit();
		for (PTInstDecl instDecl : target.getPTInstDecls()) {
			PTTemplate originator = instDecl.getTemplate();
			if (originator == null)
				continue; // error caught elsewhere
			CompilationUnit cunit = originator.getCompilationUnit();
			if (cunit == ownCU)
				continue;
			for (ImportDecl id : cunit.getImportDeclList()) {
				if (!target.hasImportDecl(id.toString())) {
					ImportDecl copy = (ImportDecl) id.copy();
					ownCU.addImportDecl(copy);
				}
			}
		}
	}

	HashMap<String, LinkedList<PTDummyClass>> getClassNamesWithDummyList() {
		HashMap<String, LinkedList<PTDummyClass>> nameAndDummies = new HashMap<String, LinkedList<PTDummyClass>>();

		for (PTInstDecl templateInst : target.getPTInstDecls()) {
			for (PTDummyClass dummy : templateInst.getPTDummyClassList()) {
				String name = dummy.getID();
				if (!nameAndDummies.containsKey(name))
					nameAndDummies.put(name, new LinkedList<PTDummyClass>());
				LinkedList<PTDummyClass> dummies = nameAndDummies.get(name);
				dummies.add(dummy);
			}
		}
		return nameAndDummies;
	}
}
