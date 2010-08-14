package javaworld;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Set;

import AST.Access;
import AST.BodyDecl;
import AST.ClassDecl;
import AST.CompilationUnit;
import AST.ConstructorDecl;
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
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

public class PTDeclRew {

	private final PTDecl target;
	private Multimap<String, PTDummyClass> nameAndDummies;
	private ImmutableList<SimpleClassRew> simpleClasses;

	public PTDeclRew(PTDecl target) {
		this.target = target;
		nameAndDummies = getClassNamesWithDummyList();
		Builder<SimpleClassRew> lb = ImmutableList.builder();
		for (SimpleClass decl : target.getSimpleClassList()) 
			lb.add(new SimpleClassRew(decl, nameAndDummies));
		simpleClasses = lb.build();
	}

	protected void addSimpleTemplateConstructorCalls() {
		for (SimpleClassRew decl : simpleClasses)
			decl.addSimpleTemplateConstructorCalls();
	}

	protected void flushCaches() {
		target.flushCaches();
	}

	protected void copyImportDecls() {
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

	protected void extendAddClassesWithInstantiatons() {
		for (SimpleClassRew decl : simpleClasses) {
			decl.extendClass();
		}
	}

	protected void createEmptyMissingAddClasses() {
		Set<String> addClasses = target.getAdditionClassNamesSet();
		Set<String> missingAddsClass = Sets.difference(nameAndDummies.keySet(),
				addClasses);
		
		Builder<SimpleClassRew> lb = ImmutableList.builder();
		lb.addAll(simpleClasses);
		for (String name : missingAddsClass) {
			ClassDecl cls = new ClassDecl(new Modifiers(), name, new Opt<Access>(),
					new List<Access>(), new List<BodyDecl>());
			PTClassAddsDecl addClass = new PTClassAddsDecl(cls);
			target.addSimpleClass(addClass);
			lb.add(new SimpleClassRew(addClass, nameAndDummies));
		}
		simpleClasses = lb.build();
	}

	private Multimap<String, PTDummyClass> getClassNamesWithDummyList() {
		Multimap<String, PTDummyClass> nameAndDummies = HashMultimap.create();
		for (PTInstDecl templateInst : target.getPTInstDecls()) {
			for (PTDummyClass dummy : templateInst.getPTDummyClassList()) {
				nameAndDummies.put(dummy.getID(), dummy);
			}
		}
		return nameAndDummies;
	}
}
