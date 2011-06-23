package javaworld;

import testutils.utils.CriticalPTException;

import java.util.Collection;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;
import java.util.Iterator;
import java.util.HashSet;

import java.util.Map;
import java.util.HashMap;

import AST.PTInterfaceAddsDecl;
import AST.SimpleSet;
import AST.PTInterfaceDecl;
import AST.Access;
import AST.BodyDecl;
import AST.ClassDecl;
import AST.ClassAccess;
import AST.CompilationUnit;
import AST.ImportDecl;
import AST.List;
import AST.Modifier;
import AST.Modifiers;
import AST.Opt;
import AST.PTClassAddsDecl;
import AST.PTClassDecl;
import AST.PTDecl;
import AST.PTInstDecl;
import AST.PTInstTuple;
import AST.PTTemplate;
import AST.PTPackage;
import AST.SimpleClass;
import AST.TypeDecl;
import AST.EnumDecl;
import AST.PTEnumDecl;
import AST.TypeVariable;
import AST.TypeAccess;
import AST.ParTypeAccess;
import AST.InterfaceDecl;
import AST.GenericInterfaceDecl;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.Multimap;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.google.common.collect.Iterables;

/** TODO after my work on interfaces:
  *   - check for various illegal situations such as
  *     merging interfaces with classes
  *   - normalize the names of things that are now
  *     referred to as "classes" in method names but
  *     could really also be interfaces
  *     (e.g. destinationClassIDsWithInstTuples
  *     and friends).
  * -svk
  */


public class PTDeclRew {

    private ParameterRewriter paramRewriter;

	private final PTDecl ptDeclToBeRewritten;

	private ImmutableList<SimpleClassRew> simpleClasses;

	public PTDeclRew(PTDecl ptDeclToBeRewritten) {
		this.ptDeclToBeRewritten = ptDeclToBeRewritten;
	}

	protected void flushCaches() {
		ptDeclToBeRewritten.flushCaches();
	}

	protected void copyImportDecls() {
		CompilationUnit ownCU = ptDeclToBeRewritten.getCompilationUnit();
		for (PTInstDecl instDecl : ptDeclToBeRewritten.getPTInstDecls()) {
			PTTemplate originator = instDecl.getTemplate();
			if (originator == null)
				continue; // error caught elsewhere
			CompilationUnit cunit = originator.getCompilationUnit();
			if (cunit == ownCU)
				continue;
			for (ImportDecl id : cunit.getImportDeclList()) {
				if (!ptDeclToBeRewritten.hasImportDecl(id.toString())) {
					ImportDecl copy = (ImportDecl) id.copy();
					ownCU.addImportDecl(copy);
				}
			}
		}
	}

    /** Create enums 'inherited' from templates, with their new names.
     */

    protected void createRenamedEnums() {
        for( String name : getDestinationIDsForEnums() ) {
            ptDeclToBeRewritten.getPTEnumDeclList().add( getRenamedEnumByName( name ) );
        }
    }

	protected void createMergedInterfaces() {
		Multimap<String, PTInstTuple> destinationClassIDsWithInstTuples = getDestinationClassIDsWithInstTuples();
        Set<String> addInterfaces = ptDeclToBeRewritten.getAdditionInterfaceNamesSet();
        Set<String> missingAddsInterfaceNames = Sets.difference( getDestinationIDsForInterfaces(), addInterfaces );

        for(String name : missingAddsInterfaceNames ) {
            PTInterfaceAddsDecl ptiad = new PTInterfaceAddsDecl( new Modifiers(),
                                                                 name,
                                                                 new List<Access>(),
                                                                 new List<BodyDecl>() );
            ptDeclToBeRewritten.addPTInterfaceDecl( ptiad );
        }


        for( String name : getDestinationIDsForInterfaces() ) {
            Collection<PTInstTuple> ituples = getDestinationClassIDsWithInstTuples().get( name );
            PTInterfaceAddsDecl target = ptDeclToBeRewritten.lookupAddsInterface( name );
            TypeConstraint otc = JastaddTypeConstraints.fromInterfaceDecl( target );
            TypeConstraint tc = new TypeConstraint();
            boolean hadAddsInteface = !missingAddsInterfaceNames.contains( name );
            boolean printDebugStuff = false;

            for(PTInstTuple ituple : ituples) {
                InterfaceDecl idecl = new InstTupleRew( ituple ).getRenamedSourceInterface();
                tc.absorb( JastaddTypeConstraints.fromInterfaceDecl( idecl ) );
            }

            for(Iterator<MethodDescriptor> it = tc.getMethodsIterator(); it.hasNext(); ) {
                MethodDescriptor method = it.next();

                if( otc.hasMethod( method ) ) continue;

                target.addMemberMethod( JastaddTypeConstraints.simpleToMethodDecl( method ) );
            }

            if( printDebugStuff ) {
                int sources = ituples.size();
                System.out.print( "From " + sources + " sources ");
                if( hadAddsInteface ) {
                    System.out.print( "plus adds class " );
                }
                System.out.println( "created interface " + name + ": " + target );
            }
        }
    }


	/**
	 * Needs extended classes in correct order. Minit dependencies are inherited
	 * and therefore a superclass must be extended before its child.
	 */
	protected void extendAddClassesWithInstantiatons() {
		Set<String> visited = Sets.newHashSet();
        boolean didMakeProgress;
		while (visited.size() < simpleClasses.size()) {
            // This loop is a bit of a wart as it's apt to go infinite-loop
            // during testing if something is wrong elsewhere so its
            // assumptions fail, which is confusing.
            // added the didMakeProgress to fail instead of looping infinitely.
            // Better ideas welcome.
            // [example of fail: ExtendExternal test]

            didMakeProgress = false;

			for (SimpleClassRew decl : simpleClasses) {
				String superName = decl.getSuperClassname();
				if (!visited.contains(decl.getName())) {
                    try {
                        if ( superName == null
                            || visited.contains(superName)
                            || decl.hasExternalSuperclass() ) {
                            visited.add(decl.getName());
                            didMakeProgress = true;
                            decl.extendClass(getDestinationClassIDsWithInstTuples(),
                                             getParameterRewriter());
                        }
                    }
                    catch( CriticalPTException e ) {
                        ptDeclToBeRewritten.error( "internal compiler error (extendAddClassesWithInstantiatons() is confused and would fail with exception)" );
                        return;
                    }
				}
			}

            if( !didMakeProgress ) {
                /* Confusingly, throwing a CriticalPTException is not in itself an indication
                   of an error. This is perhaps for the best since it means we must call
                   .error() with something more descriptive to be output to the screen.
                */
                ptDeclToBeRewritten.error( "internal compiler error (extendAddClassesWithInstantiatons() is confused and would loop infinitely)" );
                return; // TODO: this will probably lead to some confusing error messages
//                throw new CriticalPTException( "extendAddClassesWithInstantiatons() is confused -- would loop infinitely" );
            }

		}
	}

    // XXX note that in the next few methods we only test the first element.
    //     it is assumed that all the types are equal -- trying to merge an
    //     interface with a class is obviously nonsensical and an error.

    /** Get IDs for the destination _enums_.
      */
    protected Set<String> getDestinationIDsForEnums() {
		Multimap<String, PTInstTuple> destinationClassIDsWithInstTuples = getDestinationClassIDsWithInstTuples();
        Set<String> rv = new TreeSet<String>();
        for( String x : destinationClassIDsWithInstTuples.keySet() ) {
            TypeDecl typeDecl = destinationClassIDsWithInstTuples.get(x).iterator().next().getOriginator();
            if( typeDecl instanceof EnumDecl ) {
                rv.add( x );
            }
        }
        return rv;
    }

    protected Map<String,ClassDecl> getDestinationForExtendingExternals() {
		Multimap<String, PTInstTuple> destinationClassIDsWithInstTuples = getDestinationClassIDsWithInstTuples();
        Map<String,ClassDecl> rv = new HashMap<String,ClassDecl> ();
        for( String x : destinationClassIDsWithInstTuples.keySet() ) {
            TypeDecl typeDecl = destinationClassIDsWithInstTuples.get(x).iterator().next().getOriginator();
            if( typeDecl instanceof EnumDecl ) continue;
            if( typeDecl instanceof ClassDecl ) {
                ClassDecl cd = (ClassDecl) typeDecl;
                if( cd.getModifiers().isExtendsExternal() ) {
                    rv.put( x, cd );
                }
            }
        }
        return rv;
    }


    /** Get IDs for the destination _classes_ -- that is, stripping away
      * the IDs of any interfaces (and enums, although they are technically
      * classdecls) among the renamed elements.
      */

    protected Set<String> getDestinationIDsForNonEnumClasses() {
		Multimap<String, PTInstTuple> destinationClassIDsWithInstTuples = getDestinationClassIDsWithInstTuples();
        Set<String> rv = new TreeSet<String>();
        for( String x : destinationClassIDsWithInstTuples.keySet() ) {
            TypeDecl typeDecl = destinationClassIDsWithInstTuples.get(x).iterator().next().getOriginator();
            if( typeDecl instanceof EnumDecl ) continue;
            if( typeDecl instanceof ClassDecl ) {
                rv.add( x );
            }
        }
        return rv;
    }

    /** Get IDs for the destination _interfaces_.
      */

    protected Set<String> getDestinationIDsForInterfaces() {
		Multimap<String, PTInstTuple> destinationClassIDsWithInstTuples = getDestinationClassIDsWithInstTuples();
        Set<String> rv = new TreeSet<String>();
        for( String x : destinationClassIDsWithInstTuples.keySet() ) {
            if( destinationClassIDsWithInstTuples.get(x).iterator().next().getOriginator() instanceof PTInterfaceDecl ) {
                rv.add( x );
            }
        }
        return rv;
    }

    /** Get a renamed enum by (new) name.
        
        Note: this assumes that enums cannot be merged. That's reasonable, I think?
     */

    protected PTEnumDecl getRenamedEnumByName(String name) {
		Multimap<String, PTInstTuple> destinationClassIDsWithInstTuples = getDestinationClassIDsWithInstTuples();
        Set<String> rv = new TreeSet<String>();
        PTInstTuple tup = Iterables.getOnlyElement( destinationClassIDsWithInstTuples.get(name));
        return new InstTupleRew(tup).getRenamedSourceEnum();
    }

    /** Get a renamed interface by (new) name.

        Note: this assumes that interfaces cannot be merged. This is an invalid assumption, do not use. (Method to be removed.)
     */

    protected PTInterfaceDecl getRenamedInterfaceByName(String name) {
		Multimap<String, PTInstTuple> destinationClassIDsWithInstTuples = getDestinationClassIDsWithInstTuples();
        Set<String> rv = new TreeSet<String>();
        PTInstTuple tup = Iterables.getOnlyElement( destinationClassIDsWithInstTuples.get(name));
        return new InstTupleRew(tup).getRenamedSourceInterface();
    }

	protected void createEmptyMissingAddClasses() {

		Multimap<String, PTInstTuple> destinationClassIDsWithInstTuples = getDestinationClassIDsWithInstTuples();
		Builder<SimpleClassRew> lb = ImmutableList.builder();

		/*
		 * Add the explicitly defined classes residing in this PTDecl to our
		 * rewrite list.
		 */
		for (SimpleClass decl : ptDeclToBeRewritten.getSimpleClassList()) {
			lb.add(new SimpleClassRew(decl));
        }
		/*
		 * Does SimpleClassRew really need the whole
		 * destinationClassIDsWithInstTuples? Why?
		 */

		/*
		 * For those classes which are implicitly defined by inst clauses and
		 * where no concrete destination class is found, add an empty adds
		 * class. For example: package P { inst T with A => X; // no class X
		 * adds .. given later }
		 */
		Set<String> addClasses = ptDeclToBeRewritten.getAdditionClassNamesSet();
        Map<String, ClassDecl> extendingExternalsClasses = getDestinationForExtendingExternals();
		Set<String> missingAddsClass =
                Sets.difference(
                Sets.difference( getDestinationIDsForNonEnumClasses(),
                                 addClasses ),
                                 extendingExternalsClasses.keySet() );
        for(String name : extendingExternalsClasses.keySet() ) {
            Modifiers mods = new Modifiers();
            mods.addModifier( new Modifier( "extendsexternal" ) );
            ClassDecl cls = new ClassDecl(mods,
                                          name,
                                          new Opt<Access>( extendingExternalsClasses.get(name).getSuperClassAccess() ),
                                          new List<Access>(),
                                          new List<BodyDecl>());
			PTClassAddsDecl addClass = new PTClassAddsDecl(cls);
			ptDeclToBeRewritten.addSimpleClass(addClass);
			lb.add(new SimpleClassRew(addClass));
        }

		for (String name : missingAddsClass) {
            ClassDecl cls = new ClassDecl(new Modifiers(),
                                          name,
                                          new Opt<Access>(),
                                          new List<Access>(),
                                          new List<BodyDecl>());
			PTClassAddsDecl addClass = new PTClassAddsDecl(cls);

			ptDeclToBeRewritten.addSimpleClass(addClass); /* Code understanding note:
                                                             this is the line where the actual magic happens;
                                                             something is added to the AST. "simpleClasses"
                                                             just keeps track of rewriters internally which
                                                             have references to this addClass.
                                                             addSimpleClass() is a method generated by the
                                                             parser. */

			lb.add(new SimpleClassRew(addClass));
		}
		simpleClasses = lb.build();
	}

	/**
	 * returns a multimap where the key is DestinationClassID (String) and the
	 * value is a list of InstTuples (for example A => X). Per key there may be
	 * more than one InstTuple if we are merging several source classes.
	 */
	private Multimap<String, PTInstTuple> getDestinationClassIDsWithInstTuples() {
		Multimap<String, PTInstTuple> destinationClassIDsWithInstTuples = HashMultimap
				.create();
		for (PTInstDecl templateInst : ptDeclToBeRewritten.getPTInstDecls()) {
			for (PTInstTuple instTuple : templateInst.getPTInstTupleList()) {
				destinationClassIDsWithInstTuples.put(instTuple.getID(),
						instTuple);
			}
		}
		return destinationClassIDsWithInstTuples;
	}

    public boolean isPackage() {
        return ptDeclToBeRewritten instanceof PTPackage;
    }

    public boolean isTemplate() {
        return ptDeclToBeRewritten instanceof PTTemplate;
    }

	public void createInitIfPackage() {
        if( !isPackage() ) return;
        String dummyName = addDummyClass();
        for (SimpleClassRew x : simpleClasses) {
            if( !x.inheritsFromExtendsExternal() ) {
                x.createInitConstructor(dummyName);
                x.createDummyConstructor(dummyName);
            }
        }
	}

	private String addDummyClass() {
		String dummyName = "DUMMY$"; // TODO something better?
		ClassDecl dummy = new ClassDecl(new Modifiers(), dummyName, new Opt(),
				new List(), new List());
		ptDeclToBeRewritten.getSimpleClassList().add(new PTClassDecl(dummy));
		return dummyName;
	}

    /* Find, by destination name, a class being instantiated within a
       template being instantiated (this is not the same as the class this
       class will end up being copied into). This class is within the template
       part of the AST and should not be mutated. It is useful for
       resolving self-references in type parameters.
    */

    public ClassDecl getPrecopyClass( String className ) {
        for (PTInstDecl templateInst : ptDeclToBeRewritten.getPTInstDecls()) {
            for(PTInstTuple instTup : templateInst.getPTInstTuples()) {
                if( !instTup.getID().equals( className ) ) continue;

                SimpleSet typeDecs = templateInst.getTemplate().lookupType( instTup.getOrgID() );

                Iterator<TypeDecl> i = typeDecs.iterator();
                // ambiguity is really an error, but should be dealt with elsewhere
                while( i.hasNext() ) {
                    TypeDecl td = i.next();
                    if( td instanceof ClassDecl ) {
                        return (ClassDecl) td;
                    }
                }
            }
        }
        return null;
    }

    /* Find, by destination name, an interface being instantiated within a
       template being instantiated. See equivalent for classes above.
    */

    public InterfaceDecl getPrecopyInterface( String className ) {
        for (PTInstDecl templateInst : ptDeclToBeRewritten.getPTInstDecls()) {
            for(PTInstTuple instTup : templateInst.getPTInstTuples()) {
                if( !instTup.getID().equals( className ) ) continue;

                SimpleSet typeDecs = templateInst.getTemplate().lookupType( instTup.getOrgID() );

                Iterator<TypeDecl> i = typeDecs.iterator();
                // ambiguity is really an error, but should be dealt with elsewhere
                while( i.hasNext() ) {
                    TypeDecl td = i.next();
                    if( td instanceof InterfaceDecl ) {
                        return (InterfaceDecl) td;
                    }
                }
            }
        }
        return null;
    }


    /* Add the extended classes of a class (we consider a class as extending itself)
       to a set. This is nontrivial because the classes extended might not be
       local yet -- classes referenced may be inside templates, in which case the
       class in the template should be returned. (As such, this may not give
       the expected results post-copy.) This is useful for self-referencing
       type parameters.
    */

    public void precopyAddExtendedClassesOf( ClassDecl classDecl, Set<ClassDecl> extendedClasses ) {
        boolean didTryTemplateLookup = false;
        extendedClasses.add( classDecl );
        ClassDecl x = classDecl;
        while( x.hasSuperclass() ) {
            if( !x.superclass().isUnknown() ) {
                x = x.superclass();
                extendedClasses.add( x );
            } else if( !didTryTemplateLookup ) {
                // lookup failed. try by name.. but only once (if this works, any further references won't be to something instantiated in this ptdecl)
                ClassDecl cd = getPrecopyClass( x.getSuperClassName() );
                if( cd == null ) {
                    // certainly an error, but should probably be caught/reported somewhere else.
                    break;
                }
                x = cd; // continue exploring superclasses
                extendedClasses.add( cd );
                didTryTemplateLookup = true;
            } else {
                classDecl.warning( "compiler confused while computing pre-copy superclasses" );
                break;
            }
        }
    }

    public ParameterRewriter getParameterRewriter() {
        if( paramRewriter == null ) {
            ParameterRewriter pr = new ParameterRewriter();

            for (PTInstDecl templateInst : ptDeclToBeRewritten.getPTInstDecls()) {
                PTTemplate ptt = templateInst.getTemplate();

                List formalp = ptt.getTypeParameterList();
                List actualp = templateInst.getTypeArgumentList();


                if( formalp.getNumChild() != actualp.getNumChild() ) {
                    templateInst.error( "arity mismatch when instantiating " + templateInst.getID() + ": takes " + formalp.getNumChild() + " type argument(s), not " + actualp.getNumChild() );
                }

                Iterator fpi = formalp.iterator();
                Iterator api = actualp.iterator();
                
                int argcount = 0;

                // this loop is basically zipWith, more elegant way to write in Java?
                while( fpi.hasNext() && api.hasNext() ) {
                    TypeVariable fparam = (TypeVariable) fpi.next();
                    TypeAccess aparam = (TypeAccess) api.next();
                    argcount++;

                    TypeDecl tdecl = aparam.decl();

                    // check constraints:
                    //   - if fparam claims to inherit from a class C,
                    //     tdecl must represent a class V and V must
                    //     inherit from C.
                    //   - for any interface I fparam claims to implement,
                    //     tdecl must represent a class C and C must
                    //     implement I

                    // this may be complicated by the fact that tdecl
                    // might be either a pt-class or a non-pt class. (?)
                    // (never seems to be a SimpleClass, always (rewritten
                    //  to a?) plain ClassDecl. figure out why, make
                    //  sure this is not dangerously ordering-dependent)
                    if( !(tdecl instanceof ClassDecl) ) {
                        templateInst.error( "when instantiating " + templateInst.getID() + ", argument " + argcount + " does not satisfy constraints: does not refer to a class" );
                        continue;
                    }
                    ClassDecl classDecl = (ClassDecl) tdecl;

                    HashSet<ClassDecl> extendedClasses = new HashSet<ClassDecl>();
                    precopyAddExtendedClassesOf( classDecl, extendedClasses );

                    HashSet<InterfaceDecl> implementedInterfaces = new HashSet<InterfaceDecl>();
                    for( Access a : classDecl.getImplementsList() ) {
                        if( a instanceof TypeAccess ) {
                            TypeAccess ta = (TypeAccess) a;
                            String name = ta.name();
                            TypeDecl td = ta.decl();
                            if( td.isUnknown() ) {
                                InterfaceDecl id = getPrecopyInterface( name );
                                if( id != null ) {
                                    implementedInterfaces.add( id );
                                }
                            } else if( td instanceof InterfaceDecl ) {
                                implementedInterfaces.add( (InterfaceDecl) td );
                            } else {
                                // implementing non-interface. error elsewhere.
                            }
                        } else if( a instanceof ParTypeAccess ) {
                            ParTypeAccess pta = (ParTypeAccess) a;
                            TypeDecl td = pta.genericDecl();
                            if( !td.isUnknown() && td instanceof GenericInterfaceDecl ) {
                                // TODO: what are we actually to do with this GenericInterfaceDecl?
                                //       what we probably want is what is usually in .implementedInterfaces(),
                                //       which is a ParInterfaceDecl (parametrized interface decl?).
                                //       requires further study of interfaces (and I think this
                                //       is more or less the same problem as issue 13)
                                implementedInterfaces.add( (InterfaceDecl) td );
                            }
                        } else {
                            classDecl.warning( "compiler confused -- something unexpected in implements-list: " + a.getClass().getName() );
                        }
                    }

                    boolean okay = true;

                    for( Access constraintAcc : fparam.getTypeBoundList() ) {
                        TypeDecl constraint = constraintAcc.type();
                        if( constraint.isClassDecl() ) {
                            if( !extendedClasses.contains( constraint ) ) {
                                templateInst.error( "when instantiating " + templateInst.getID() + ", argument " + argcount + " does not satisfy constraints: does not extend class " + constraint.fullName() );
                                okay = false;
                            }
                        }
                        if( constraint.isInterfaceDecl() ) {
                            if( !implementedInterfaces.contains( constraint ) ) {
                                templateInst.error( "when instantiating " + templateInst.getID() + ", argument " + argcount + " does not satisfy constraints: does not implement interface " + constraint.fullName() );
                                okay = false;
                            }
                        }
                    }

                    // we try to add as many rewrites as possible, even if
                    // we've encountered errors along the way.
                    // this seems to give more informative error messages
                    // (and eliminates in some but not all cases confusing
                    //  ones about there being no visible type with the
                    //  type variable name (in the generated Java AST))

                    pr.addRewrite( fparam, aparam );
                }
            }

            paramRewriter = pr;
        }
        assert( paramRewriter != null );
        return paramRewriter;
    }
}
