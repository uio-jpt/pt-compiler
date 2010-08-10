package javaworld;

import java.util.Collection;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;

import AST.Access;
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
import AST.TypeAccess;

import com.google.common.base.Joiner;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

public class PTDeclRew {

	private final PTDecl target;
	private Multimap<String, PTDummyClass> nameAndDummies;

	public PTDeclRew(PTDecl target) {
		this.target = target;
		nameAndDummies = getClassNamesWithDummyList();
	}

	public void flushCaches() {
		target.flushCaches();
	}

	public void updateAddsSuperClasses() {
		for (PTClassAddsDecl decl : target.getAdditionClassList()) {
			if (nameAndDummies.containsKey(decl.getID()))
				// error check elsewhere
				updateSuperName(decl);
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

	public void updateSuperName(PTClassAddsDecl decl) {
		HashSet<String> superNames = Sets.newHashSet();
		for (PTDummyClass dummy : nameAndDummies.get(decl.getID()))
			superNames.add(dummy.getRenamedSuperclassName());
		superNames.remove(null); // classes without superclass
		if (superNames.size() > 1) {
			decl.error(String.format(
					"Merge error for %s. superklasses %s must be merged.\n",
					decl.getID(), Joiner.on(" and ").join(superNames)));
		} else {
			try {
				decl.getClassDecl().setSuperClassAccess(
						new TypeAccess(Iterables.getOnlyElement(superNames)));
			} catch (NoSuchElementException e) { // no superclasses
			} 
		}
	}

	public String getTemplateName(String superClassName, String methodName) {
		Collection<String> templates = Lists.newLinkedList();
		for (PTInstDecl templateInst : target.getPTInstDecls()) {
			for (PTDummyClass dummy : templateInst.getPTDummyClassList()) {
				DummyRew x = new DummyRew(dummy);
				if (x.sourceClassHasNameAndMethod(superClassName, methodName))
					templates.add(templateInst.getTemplate().getID());
			}
		}
		if (templates.size() == 1) {
			return Iterables.getOnlyElement(templates);
		} else {
			target.error(String.format(
					"Ambiguous super call on method 'super[%s].%s', templates matching was "
							+ templates.toString(), superClassName, methodName));
			return "UnknownTemplate";
		}
	}

	protected void extendAddClassesWithInstantiatons() {
		for (SimpleClass decl : target.getSimpleClassList()) {
			SimpleClassRew rDecl = new SimpleClassRew(decl, nameAndDummies);
			rDecl.extendClass();
		}
	}

	protected void createEmptyMissingAddClasses() {
		Set<String> addClasses = target.getAdditionClassNamesSet();
		Set<String> missingAddsClass = Sets.difference(nameAndDummies.keySet(),
				addClasses);
	
		for (String name : missingAddsClass) {
			ClassDecl cls = new ClassDecl(new Modifiers(), name, new Opt<Access>(),
					new List<Access>(), new List<BodyDecl>());
			PTClassAddsDecl addClass = new PTClassAddsDecl(cls);
			target.addSimpleClass(addClass);
		}
	}

	private Multimap<String, PTDummyClass> getClassNamesWithDummyList() {
		Multimap<String, PTDummyClass> nameAndDummies = ArrayListMultimap
				.create();
		for (PTInstDecl templateInst : target.getPTInstDecls()) {
			for (PTDummyClass dummy : templateInst.getPTDummyClassList()) {
				nameAndDummies.put(dummy.getID(), dummy);
			}
		}
		return nameAndDummies;
	}
}
