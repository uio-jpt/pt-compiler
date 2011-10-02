package javaworld;

import testutils.utils.CriticalPTException;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import AST.Access;
import AST.Block;
import AST.BodyDecl;
import AST.ClassDecl;
import AST.InterfaceDecl;
import AST.ClassInstanceExpr;
import AST.ConstructorDecl;
import AST.Expr;
import AST.ExprStmt;
import AST.List;
import AST.Modifiers;
import AST.Opt;
import AST.PTInstTuple;
import AST.PTTemplate;
import AST.PTPackage;
import AST.PTClassAddsDecl;
import AST.PackageConstructor;
import AST.ParameterDeclaration;
import AST.SimpleClass;
import AST.SimpleSet;
import AST.Stmt;
import AST.SuperConstructorAccess;
import AST.TemplateConstructorAccess;
import AST.TypeAccess;
import AST.ParTypeAccess;
import AST.TypeDecl;
import AST.VarAccess;
import AST.Modifier;
import AST.MethodDecl;
import AST.PTDecl;
import AST.PTConstructorDecl;
import AST.PTTSuperConstructorCall;
import AST.MethodAccess;
import AST.PTConstructorPromise;

import com.google.common.base.Joiner;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;

public class SimpleClassRew {
	private final SimpleClass decl;
	private Collection<PTInstTuple> instTuples;
	private Set<String> possibleConflicts;
	private Collection<ClassDeclRew> renamedSources; /* renaming may be trivial */

	public SimpleClassRew(SimpleClass decl) {
		this.decl = decl;
	}

	/**
	 * Extends a single class with the instantiations given in the current
	 * scope.
	 */
	public void extendClass(
			Multimap<String, PTInstTuple> destinationClassIDsWithInstTuples,
            ParameterRewriter parameterRewriter
            ) {
		if (!checkIfSane(destinationClassIDsWithInstTuples)) {
			return;
		}
		instTuples = destinationClassIDsWithInstTuples.get(decl.getID());
		renamedSources = getRenamedInstClassesRewriters();
		possibleConflicts = getPossibleConflicts();
		computeClassToTemplateMultimap();
		updateSuperName();
        updateImplementsNames();
		computeTSuperDeps(); // the effect of this is to extend getClassDecl().allTDeps
                            

        updateAbstractness();

        for (ClassDeclRew source : renamedSources) {
            source.applyMutator( parameterRewriter );
        }

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
		for (PTInstTuple instTuple : sorted(instTuples)) {
            TypeDecl x = instTuple.getOriginator();
            if( x instanceof ClassDecl ) {
                expandDepsWith(deps, instTuple.getTemplate().getID(),
                       (ClassDecl) x );
            }
		}
		LinkedHashMap<String, String> superDeps = getSuperDepsCopy();
		for (String superDep : superDeps.keySet()) {

			if (!deps.containsKey(superDep)) {
				String value = superDeps.get(superDep);
				deps.put(superDep, value);
			}
		}
	}

	/**
	 * Created to ensure a stable ordering of non-interfering elements.
	 * Used to support the testscript.
	 * @param x
	 * @return
	 */
	private Iterable<PTInstTuple> sorted(Collection<PTInstTuple> x) {
		Comparator<PTInstTuple> byName = new Comparator<PTInstTuple>() {
			
			@Override
			public int compare(PTInstTuple o1, PTInstTuple o2) {
				String x = instName(o1);
				String y = instName(o2);
				return x.compareTo(y);
			}
		};
		return Ordering.from(byName).immutableSortedCopy(x);
	}

	protected String instName(PTInstTuple x) {
		return String.format("%s.%s",x.getTemplate().getID(),x.getOrgID());
	}

	private void expandDepsWith(LinkedHashMap<String, String> deps,
			String templateName, ClassDecl originator) {
		deps.putAll(originator.allTDeps);
        String tmSuperName;
//        try {
            tmSuperName = originator.getTopMostInternalSuperName();
//        }
//        catch( NullPointerException e ) {
//            throw new CriticalPTException( "internal failure in expandDepsWith() [assumption failed]" );
//        }
		String key = Util.toMinitName(templateName, tmSuperName );
		String value = Util.toMinitName(templateName, originator.getID());
		deps.put(key, value);
	}

	private LinkedHashMap<String, String> getSuperDepsCopy() {
		String superName = decl.getClassDecl().getSuperClassName();
		if (!decl.getClassDecl().getModifiers().isExtendsExternal() && superName != null) {
            SimpleClass sc = decl.getPTDecl().getSimpleClass(superName);
            if( sc == null ) {
                decl.error( "cannot find internal superclass " + superName + "(did you mean \"extends external\"?)");
                return Maps.newLinkedHashMap();
            }
            return sc.getClassDecl().allTDeps;
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
			String classID = dummy.getOrgID();
			String templateID = dummy.getTemplate().getID();
			classToTemplates.put(classID, templateID);
		}
		decl.getClassDecl().setClassToTemplateMap(classToTemplates);
	}

    /** Updates the modifiers of this class such that it is set to abstract
      * if _any_ of the merged classes are abstract. Note that the class
      * stays abstract if it is already abstract, but all the merged classes
      * are concrete. */
    private void updateAbstractness() {
        if( !decl.getClassDecl().isAbstract() ) {
            boolean shouldBeAbstract = false;
            for( ClassDeclRew x : renamedSources ) {
                if( x.isAbstract() ) {
                    shouldBeAbstract = true;
                }
            }

            if( shouldBeAbstract ) {
                decl.getClassDecl().getModifiers().addModifier( new Modifier( "abstract" ) );
            }
        }
    }

    /**
      * Updates the implemented interfaces of this class to the union of the
      * implemented interfaces of the merged classes.
      */
    private void updateImplementsNames() {
        HashSet<JastaddTypeDescriptor> accessSet = Sets.newHashSet();


        for( Object o : decl.getClassDecl().getImplementsList() ) {
            if( o instanceof TypeAccess ) {
                TypeAccess ta = (TypeAccess) o;
                accessSet.add( new JastaddTypeDescriptor( ta ) );
            } else if( o instanceof ParTypeAccess ) {
                ParTypeAccess pta = (ParTypeAccess) o; // parametrized type
                accessSet.add( new JastaddTypeDescriptor( pta ) );
            } else {
                decl.error( "internal compiler error: in implements-list, encountered unexpected type " + o.getClass().getName() );
            }
        }
		for (ClassDeclRew x : renamedSources) {
            for( Access acc : x.getClassDecl().getImplementsList() ) {
                accessSet.add( new JastaddTypeDescriptor( acc ) );
            }
		}

        List<Access> accessList = new List<Access>();
        for( JastaddTypeDescriptor acc : accessSet ) {
            accessList.add( acc.getAccess() );
        }

        decl.getClassDecl().setImplementsList( accessList );
    }


	/**
	 * Sets the supername of this class to the supername of the merged classes.
	 */
	private void updateSuperName() {
		HashSet<String> names = Sets.newHashSet();
        boolean oneExternal = false;
		for (ClassDeclRew x : renamedSources) {
			names.add(x.getSuperClassName());
            ClassDecl sup = x.getClassDecl().superclass();
            if( sup != null && !sup.isPtInternalClass() ) {
                oneExternal = true;
            }
		}
		names.remove(null); // classes without superclass
        if( oneExternal && names.size() > 1 ) {
            // note that per spec we do not even check whether these all extend each other
            decl.error(String.format(
                    "Merge error for %s: distinct external superclasses %s cannot be merged\n",
                    decl.getID(), Joiner.on(" and ").join(names)));
        } else {
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

        // try to keep only nontabstracts, as tabstracts aren't conflicts in this sense
        // xx any corner cases that slip through this?
		Set<String> hostMethods = Sets.difference(decl.getClassDecl().methodSignatures(),
                                                  decl.getTabstractSignatures() );

		Set<String> hostFields = ImmutableSet.copyOf((decl.getClassDecl().fieldNames()));
		Set<String> allDefinitions = Sets.union(hostMethods,hostFields);
		
		for (ClassDeclRew renamedDecl : renamedSources) {
            Set<String> abstractInstanceDecls = new HashSet<String> ();
            // horrible, horrible hack below. class-hopping, there must be a more direct way
            for( BodyDecl bd : renamedDecl.getClassDecl().getBodyDecls() ) {
                if( bd instanceof MethodDecl ) {
                    MethodDecl md = (MethodDecl) bd;
                    if( md.isTabstract() ) {
                        abstractInstanceDecls.add( md.signature() );
                    }
                }
            }

			Set<String> instanceDecls = Sets.difference( renamedDecl.getSignatures(),
                                                         abstractInstanceDecls );

			Set<String> localCollisions = Sets.intersection(instanceDecls,
					allDefinitions);
			allDefinitions = Sets.union(allDefinitions,
					instanceDecls);
			collisions = Sets.union(collisions,
					localCollisions);
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

        boolean isInPackage = target.getParentClass( PTPackage.class ) != null;
		for (BodyDecl bodyDecl : bodyDecls) {
            boolean isConstructorDecl = bodyDecl instanceof ConstructorDecl;
            boolean isUnneededTabstractMethodDecl = false;

            if(bodyDecl instanceof MethodDecl) {
                MethodDecl meth = (MethodDecl) bodyDecl;
                if( meth.isTabstract() ) {
                    if (isInPackage
                        || target.hostType().methodsSignature( meth.signature() ) != SimpleSet.emptySet ) {
//                        System.out.println( "" + bodyDecl + " is an unneeded abstract" );
//                        System.out.println( " with sig " + meth.signature() );
                        isUnneededTabstractMethodDecl = true;
                    }

                    decl.addTabstractSignature( meth.signature() );
                }

				/* Sjekk for "unintentional override": */

				if (meth.isVirtual) {
					ClassDecl c = target.superclass();
					
					while (c != null) {
						for (BodyDecl d: c.getBodyDecls()) {
							if (d instanceof MethodDecl && meth.sameSignature((MethodDecl)d)) {

								if (decl.wasAddsClass()) {
									// TODO: Mer utfyllende sjekk her om meth faktisk ble omdefinert i decl?
									// Kan se ut som dette løser seg selv med "unresolved conflict during merging".

									/* if (sjekk ok) */ continue;
								}

								decl.error(String.format("Merging causes virtual method %s in class "+
									"%s to overload existing method in class %s.",
									meth.signature(), target.getID(), c.getID()));
                            }
						}
						c = c.superclass();
					}
				} 

            }

            if( bodyDecl instanceof PTConstructorPromise ) {
                PTConstructorPromise ptcp = (PTConstructorPromise) bodyDecl;
                bodyDecl = new PTConstructorPromise( ptcp.getModifiers().fullCopy(), target.getID(), ptcp.getParameterList().fullCopy(), ptcp.getExceptionList().fullCopy() );
            }

            if( !isConstructorDecl && !isUnneededTabstractMethodDecl) {
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
	 * ClassDecl. The renaming is done in two parts: 1. Based on new class names
	 * in the inst clause, which will rename types used. 2. Based on explicit
	 * renamings in the inst clause, which will rename methods and variables.
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
            if( x.getOriginator() instanceof ClassDecl ) {
                InstTupleRew instTupleRew = new InstTupleRew(x);
                ClassDeclRew ext = instTupleRew.getRenamedSourceClass();
                instClasses.add(ext);
            }
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
			return new Opt<Stmt>(new ExprStmt(new SuperConstructorAccess(
					"super", new List<Expr>().add(new ClassInstanceExpr(
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
		SuperConstructorAccess superaccess = (SuperConstructorAccess) cinv
				.getExpr();
		List<Expr> superCallArgs = superaccess.getArgList();

		c.setConstructorInvocationOpt(getDummySuperCall(dummyName));
		List<Stmt> origStatements = c.getBlock().getStmtList();
		List<Stmt> otherStatements = new List<Stmt>();
		LinkedList<String> deps = Lists.newLinkedList(depsMap.values());
		LinkedList<TemplateConstructorAccess> callChain = Lists.newLinkedList();

		String supername = c.getClassDecl().getSuperClassName();
		if (supername != null) {
			String methodName = Util.toMinitName(decl.getPTDecl().getID(),
					supername);
			TemplateConstructorAccess supercall = new TemplateConstructorAccess(
					methodName, superCallArgs, supername, decl.getPTDecl()
							.getID());
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
		String ownName = Util.toMinitName(decl.getPTDecl().getID(),
				decl.getID());
		PackageConstructor ownMinit = new PackageConstructor(c.getModifiers(),
				new TypeAccess("void"), ownName, c.getParameterList(),
				new List<Access>(), new Opt<Block>(new Block(otherStatements)),
				decl.getPTDecl().getID(), decl.getID());
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
		String tclassID = x.getTClassID();
		String tname = x.getTemplateID();

		for (int i = 0; i < callChain.size(); i++) {
			TemplateConstructorAccess cur = callChain.get(i);
			String curClassName = cur.getTClassID();
			String curTemplateName = cur.getTemplateID();
			if (curClassName.equals(tclassID) && curTemplateName.equals(tname)) {
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

    public boolean inheritsFromExtendsExternal() {
        ClassDecl cd = decl.getClassDecl();
        return cd.inheritsFromExtendsExternal();
    }

    public boolean isExtendsExternal() {
        ClassDecl cd = decl.getClassDecl();
        return cd.getModifiers().isExtendsExternal();
    }

    public boolean hasExternalSuperclass() {
        ClassDecl cd = decl.getClassDecl();
        return cd.hasSuperclass() && !cd.superclass().isPtInternalClass();
    }

    public void rewriteConstructorsInPackage() {
        for( ConstructorDecl cdecl : decl.getClassDecl().getConstructorDeclList() ) {
            if( !(cdecl instanceof PTConstructorDecl) ) {
                // This must be an implicit, empty constructor.
                // We have no tsuper statements to add, yet is it okay to just skip?
                // We might still need to generate an error, but that can be done elsewhere?
                System.out.println( "SKIPPING" );
                continue;
            }
            PTConstructorDecl pcdecl = (PTConstructorDecl) cdecl;
            java.util.List<Stmt> stmts = new ArrayList<Stmt>();

            for(PTTSuperConstructorCall scc : pcdecl.getTSuperConstructorInvocationList() ) {
                String superTemplateID = scc.getSuperTemplateID();
                String tsuperClassID = scc.getTemplateSuperclassID();
                String methodName = Util.toMinitName( superTemplateID, tsuperClassID );
                AST.List<Expr> args = scc.getArgs(); /// ??? --- do we need to make a copy?
                Stmt stmt = new ExprStmt( new MethodAccess( methodName, args ) );
                stmts.add( stmt );
            }

            java.util.Collections.reverse( stmts );
            for( Stmt stmt : stmts ) {
                System.out.println( "added : " + stmt );
                pcdecl.getBlock().getStmts().insertChild( stmt, 0 );
            }
        }
    }
}
