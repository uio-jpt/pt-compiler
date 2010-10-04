package javaworld;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import AST.Access;
import AST.Block;
import AST.BodyDecl;
import AST.ClassDecl;
import AST.ClassInstanceExpr;
import AST.ConstructorDecl;
import AST.Expr;
import AST.ExprStmt;
import AST.List;
import AST.Modifiers;
import AST.Opt;
import AST.PTInstTuple;
import AST.PTTemplate;
import AST.PackageConstructor;
import AST.ParameterDeclaration;
import AST.SimpleClass;
import AST.Stmt;
import AST.SuperConstructorAccess;
import AST.TemplateConstructorAccess;
import AST.TemplateConstructorAccessShort;
import AST.TypeAccess;
import AST.TypeDecl;
import AST.VarAccess;

import com.google.common.base.Joiner;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

public class SimpleClassRew {
	private final SimpleClass decl;
	private Collection<PTInstTuple> instTuples;
	private Set<String> possibleConflicts;
	private Collection<ClassDeclRew> renamedSources;

	public SimpleClassRew(SimpleClass decl) {
		this.decl = decl;
	}

	/**
	 * Extends a single class with the instantiations given in the current
	 * scope.
	 */
	public void extendClass(Multimap<String, PTInstTuple> destinationClassIDsWithInstTuples) {
		if (!checkIfSane(destinationClassIDsWithInstTuples)) {
			return;
		}
		instTuples = destinationClassIDsWithInstTuples.get(decl.getID());
		renamedSources = getRenamedInstClassesRewriters();
		possibleConflicts = getPossibleConflicts();
		computeClassToTemplateMultimap();
		updateSuperName();
		computeTSuperDeps();
	
		if (mergingIsPossible()) {
			renameResolvedConflicts();
			for (ClassDeclRew source : renamedSources) {
				addDecls(source.getBodyDecls());
			}
		}
		// decl.getClassDecl().getConstructorDeclList()
	}

	private void computeTSuperDeps() {
		LinkedHashMap<String, String> deps = decl.getClassDecl().allTDeps;
		for (PTInstTuple instTuple : instTuples) {
			expandDepsWith(deps,instTuple.getTemplate().getID(), instTuple.getOriginator());
		}
		LinkedHashMap<String, String> superDeps = getSuperDepsCopy();
		for (String superDep: superDeps.keySet()) {
			
			if (!deps.containsKey(superDep)) {
				String value = superDeps.get(superDep);
				deps.put(superDep,value);
			}
		}
	}

	private void expandDepsWith(LinkedHashMap<String, String> deps, String templateName, ClassDecl originator) {
		deps.putAll(originator.allTDeps);
		String key = Util.toMinitName(templateName,originator.getTopMostSuperName());
		String value = Util.toMinitName(templateName,originator.getID());
		deps.put(key, value);
	}

	private LinkedHashMap<String, String> getSuperDepsCopy() {
		String superName = decl.getClassDecl().getSuperClassName();
		if (superName != null) {
			return decl.getPTDecl().getSimpleClass(superName).getClassDecl().allTDeps;
		} else {
			return Maps.newLinkedHashMap();
		}
	}

	/**
	 * See instantiationrewrite.jadd
	 * 
	 * Used to expand tsuper[classname] to tsuper[templatename,classname]
	 */
	private void computeClassToTemplateMultimap() {
		Multimap<String, String> classToTemplates = HashMultimap.create();
		for (PTInstTuple dummy : instTuples) {
			String classID = dummy.getOriginator().getID();
			String templateID = dummy.getTemplate().getID();
			classToTemplates.put(classID, templateID);
		}
		decl.getClassDecl().setClassToTemplateMap(classToTemplates);
	}

	/**
	 * Sets the supername of this class to the
	 * supername of the merged classes.
	 */
	private void updateSuperName() {
		HashSet<String> names = Sets.newHashSet();
		for (ClassDeclRew x : renamedSources) {
			names.add(x.getSuperClassName());
		}
		names.remove(null); // classes without superclass
		try {
			decl.getClassDecl().setSuperClassAccess(
					new TypeAccess(Iterables.getOnlyElement(names)));
		} catch (NoSuchElementException e) { // no superclasses
		} catch (IllegalArgumentException e) {
			decl.error(String.format(
					"Merge error for %s. Superclasses %s must be merged.\n",
					decl.getID(), Joiner.on(" and ").join(names)));
		}
	}

	private boolean addsResolvesConflict() {
		Set<String> addRefinements = decl.getClassDecl().methodSignatures();
		for (String conflictingName : possibleConflicts) {
			if (!addRefinements.contains(conflictingName)) {
				decl.error(conflictingName
						+ " is an unresolved conflict during merging.\n");
				return false;
			}
		}
		return true;
	}

	/**
	 * Had a bug with views, switched to immutableSets. Code may be written more
	 * concise. Returns intersection of all renamed signatures (fields and
	 * methods) from all tsuperclasses.
	 * 
	 * (since getConflicts needs all classes renamed. maybe it should use a
	 * renamed copy for these purposes.)
	 * 
	 * @return A set of possible conflicts. 'Possible' means that an adds method
	 *         may resolve the conflict.
	 */
	private Set<String> getPossibleConflicts() {
		Set<String> collisions = ImmutableSet.of();
		Set<String> allDefinitions = ImmutableSet.copyOf((decl.getClassDecl()
				.methodSignatures()));
		for (ClassDeclRew decl : renamedSources) {
			Set<String> instanceDecls = decl.getSignatures();
			Set<String> localCollisions = Sets.intersection(instanceDecls,
					allDefinitions);
			allDefinitions = ImmutableSet.copyOf(Sets.union(allDefinitions,
					instanceDecls));
			collisions = ImmutableSet.copyOf(Sets.union(collisions,
					localCollisions));
		}
		return collisions;
	}

	private void renameResolvedConflicts() {
		for (ClassDeclRew decl : renamedSources)
			decl.renameMatchingMethods(possibleConflicts);
	}

	private boolean addsHasOwnConstructor() {
		boolean ans = decl.getClassDecl().getConstructorDeclList().size() > 0;
		if (!ans) {
			PTTemplate t = (PTTemplate) decl.getParentClass(PTTemplate.class);
			decl.error(String
					.format("Class %s in template %s is missing a constructor. Unable to merge...",
							decl.getID(), t.getID()));
		}
		return ans;
	}

	private boolean mergingIsPossible() {
		return addsResolvesConflict() && addsHasOwnConstructor();
	}

	private void addDecls(List<BodyDecl> bodyDecls) {
		ClassDecl target = decl.getClassDecl();
		for (BodyDecl bodyDecl : bodyDecls) {
			if (!(bodyDecl instanceof ConstructorDecl)) {
				target.addBodyDecl(bodyDecl);
			}
		}
	}

	/**
	 * Renamed classes are not cross checked. If there's a method name conflict
	 * that the adds class resolves, then the correct renaming of the
	 * conflicting classes will be performed later on.<br/>
	 * <br/>
	 * 
	 * The wrapper class, of which as list is returned, contains the renamed
	 * ClassDecl. The renaming is done in two parts: 
	 * 		1. Based on new class names in the inst clause, which will rename types used. 
	 * 		2. Based on explicit renamings in the inst clause, which will rename methods and variables.
	 * 
	 * This is the so-called first part of the total renaming process. The
	 * second part of the total renaming process deals with renaming conflicts
	 * between merged classes.
	 * 
	 * @return A list of rewriters wrapper classes for all classes that will be
	 *         merged into the current class.
	 */
	private Collection<ClassDeclRew> getRenamedInstClassesRewriters() {
		Collection<ClassDeclRew> instClasses = Lists.newLinkedList();
		for (PTInstTuple x : instTuples) {
			InstTupleRew instTupleRew = new InstTupleRew(x);
			ClassDeclRew ext = instTupleRew.getRenamedSourceClass();
			instClasses.add(ext);
		}
		return instClasses;
	}

	private boolean checkIfSane(
			Multimap<String, PTInstTuple> destinationClassIDsWithInstTuples) {
		if (destinationClassIDsWithInstTuples.containsKey(decl.getID())) {
			if (!decl.isAddsClass()) {
				decl.error("Class "
						+ decl.getID()
						+ " has dual roles. It's both defined as an inpedendent class and as a template adds class.\n");
				return false;
			}
		} else if (decl.isAddsClass()) {
			decl.error(decl.getID()
					+ " is an add class, template source class not found!\n");
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return decl.getClassDecl().toString();
	}

	public void createDummyConstructor(String dummyName) {
		List<ParameterDeclaration> params = new List<ParameterDeclaration>();
		params.add(new ParameterDeclaration(new TypeAccess(dummyName), "dummy"));
		Opt<Stmt> superInvo = getDummySuperCall(dummyName);
		;
		ConstructorDecl dummy = new ConstructorDecl(new Modifiers(),
				decl.getID(), params, new List<Access>(), superInvo, new Block(
						new List<Stmt>()));
		decl.getClassDecl().getBodyDeclList().add(dummy);
	}

	private Opt<Stmt> getDummySuperCall(String dummyName) {
		String superName = decl.getClassDecl().getSuperClassName();
		if (superName != null) {
			return new Opt<Stmt>(
					new ExprStmt(new SuperConstructorAccess("super",
							new List<Expr>().add(new ClassInstanceExpr(
									new TypeAccess(dummyName), new List<Expr>(),
									new Opt<TypeDecl>())))));
		} else {
			return new Opt<Stmt>();
		}
	}

	/**
	 * TODO: if real constructor has args, it'll already be renamed to a minit
	 * method. So atm only the empty regenerated constructor will show here
	 * (that's my guess anyway). For every original constructor (now minit), a
	 * real constructor with the same params should be created that first will
	 * call a init method (calls deps), and then call the the correct minit with
	 * the correct args.
	 */
	public void createInitConstructor(String dummyName) {
		ClassDecl h = decl.getClassDecl();
		for (BodyDecl bdecl : h.getBodyDeclList()) {
			if (bdecl instanceof ConstructorDecl) {
				callDummySuperAndDeps(dummyName, (ConstructorDecl) bdecl);
			}
		}
	}

	private void callDummySuperAndDeps(String dummyName, ConstructorDecl c) {
		Map<String, String> depsMap = c.getClassDecl().allTDeps;
		
		// store supercall args for later usage
		ExprStmt cinv = (ExprStmt) c.getConstructorInvocation(); 
		SuperConstructorAccess superaccess = (SuperConstructorAccess) cinv.getExpr();
		List<Expr> superCallArgs = superaccess.getArgList();
		
		c.setConstructorInvocationOpt(getDummySuperCall(dummyName));
		List<Stmt> origStatements = c.getBlock().getStmtList();
		List<Stmt> otherStatements = new List<Stmt>();
		LinkedList<String> deps = Lists.newLinkedList(depsMap.values());
		LinkedList<TemplateConstructorAccess> callChain = Lists.newLinkedList();

		String supername = c.getClassDecl().getSuperClassName();
		if (supername != null) {
			String methodName = Util.toMinitName(decl.getPTDecl().getID(), supername);
			TemplateConstructorAccess supercall = new TemplateConstructorAccess(methodName, superCallArgs, supername,decl.getPTDecl().getID());
			otherStatements = otherStatements.add(new ExprStmt(supercall));
		}
		
		// add generic calls
		for (String dep : deps) {
			String[] parts = dep.split("\\$"); // TODO let dep be a data object
			String templateID = parts[1];
			String tclassID = parts[2];
			TemplateConstructorAccess x = new TemplateConstructorAccess(dep,
					new List<Expr>(), tclassID, templateID);
			callChain.add(x);
		}
		// replace with explicit calls
		for (Stmt stmt : origStatements) {
			if (stmt instanceof ExprStmt) {
				ExprStmt exprstmt = (ExprStmt) stmt;
				Expr expr = exprstmt.getExpr();
				if (expr instanceof TemplateConstructorAccess) {
					TemplateConstructorAccess x = (TemplateConstructorAccess) expr;
					if (x instanceof TemplateConstructorAccessShort)
						replaceImplicitTSuperCall(
								(TemplateConstructorAccessShort) x, callChain);
					else
						replaceGeneric(x, callChain);
				} else {
					otherStatements.add(stmt);
				}
			}
		}

		// add modified call chain
		List<Stmt> statements = new List<Stmt>();
		for (TemplateConstructorAccess x : callChain) {
			statements = statements.add(new ExprStmt(x));
		}
		String ownName = Util.toMinitName(decl.getPTDecl().getID(), decl.getID());
		PackageConstructor ownMinit = new PackageConstructor(c.getModifiers(),
				new TypeAccess("void"), ownName,
				c.getParameterList(), new List<Access>(), new Opt<Block>(
						new Block(otherStatements)), decl.getPTDecl().getID(), decl.getID());
		decl.getClassDecl().addBodyDecl(ownMinit);
		List<Expr> args = new List<Expr>();
		for (ParameterDeclaration p : c.getParameterList()) {
			args = args.add(new VarAccess(p.getID()));
		}
		TemplateConstructorAccess x = new TemplateConstructorAccess(ownName,
				args, decl.getPTDecl().getID(), decl.getID());
		c.getBlock().setStmtList(statements.add(new ExprStmt(x)));
	}

	private void replaceGeneric(TemplateConstructorAccess x,
			LinkedList<TemplateConstructorAccess> callChain) {
		String tclassID= x.getTClassID();
		String tname = x.getTemplateID();
		
		for (int i=0; i < callChain.size(); i++) {
			TemplateConstructorAccess cur = callChain.get(i);
			String curClassName = cur.getTClassID();
			String curTemplateName = cur.getTemplateID();
			if (curClassName.equals(tclassID) && curTemplateName.equals(tname)) {
				callChain.set(i, x);
			}
		}

	}

	private void replaceImplicitTSuperCall(TemplateConstructorAccessShort x,
			LinkedList<TemplateConstructorAccess> callChain) {
		String name = x.getTClassID();
		for (int i=0; i < callChain.size(); i++) {
			TemplateConstructorAccess cur = callChain.get(i);
			if (cur.getTClassID().equals(name)) {
				callChain.set(i, x);
			}
		}
	}


	public String getSuperClassname() {
		return decl.getClassDecl().getSuperClassName();
	}

	public String getName() {
		return decl.getID();
	}
}
