package javaworld;

import java.util.Set;

import AST.Access;
import AST.BodyDecl;
import AST.ClassDecl;
import AST.CompilationUnit;
import AST.ImportDecl;
import AST.List;
import AST.Modifier;
import AST.Modifiers;
import AST.Opt;
import AST.PTC;
import AST.PTClassAddsDecl;
import AST.PTClassDecl;
import AST.PTDecl;
import AST.PTInstDecl;
import AST.PTInstTuple;
import AST.PTTemplate;
import AST.SimpleClass;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

public class PTDeclRew {

	private final PTDecl target;
	private Multimap<String, PTInstTuple> destinationClassIDsWithInstTuples;
	private ImmutableList<SimpleClassRew> simpleClasses;

	public PTDeclRew(PTDecl target) {
		this.target = target;
		destinationClassIDsWithInstTuples = getDestinationClassIDsWithInstTuples();
		Builder<SimpleClassRew> lb = ImmutableList.builder();
		for (SimpleClass decl : target.getSimpleClassList()) 
			lb.add(new SimpleClassRew(decl, destinationClassIDsWithInstTuples));
		simpleClasses = lb.build();
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
		Set<String> missingAddsClass = Sets.difference(destinationClassIDsWithInstTuples.keySet(),
				addClasses);
		
		Builder<SimpleClassRew> lb = ImmutableList.builder();
		lb.addAll(simpleClasses);
		for (String name : missingAddsClass) {
			ClassDecl cls = new PTC(new Modifiers(), name, new Opt<Access>(),
					new List<Access>(), new List<BodyDecl>());
			PTClassAddsDecl addClass = new PTClassAddsDecl(cls);
			target.addSimpleClass(addClass);
			lb.add(new SimpleClassRew(addClass, destinationClassIDsWithInstTuples));
		}
		simpleClasses = lb.build();
	}

	/**
	 * returns a multimap where the 
	 * 		key is DestinationClassID (String) 
	 * 		and the value is a list of InstTuples (for example A => X). 
	 */
	private Multimap<String, PTInstTuple> getDestinationClassIDsWithInstTuples() {
		Multimap<String, PTInstTuple> nameAndDummies = HashMultimap.create();
		for (PTInstDecl templateInst : target.getPTInstDecls()) {
			for (PTInstTuple dummy : templateInst.getPTInstTupleList()) {
				nameAndDummies.put(dummy.getID(), dummy);
			}
		}
		return nameAndDummies;
	}

	public void createInitIfPackage() {
		if (target instanceof PTTemplate) return;
		String dummyName = addDummyClass();
		for (SimpleClassRew x : simpleClasses) {
			x.addConstructors(dummyName);
		}
	}

	private String addDummyClass() {
		
		String dummyName = "DUMMY$"; // TODO something better?
		ClassDecl dummy = new ClassDecl(new Modifiers(), dummyName, new Opt(), new List(), new List());
		target.getSimpleClassList().add(new PTClassDecl(dummy));
		return dummyName;
	}
}
