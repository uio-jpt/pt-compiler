package javaworld;

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

import AST.PTInterfaceDecl;
import AST.Access;
import AST.BodyDecl;
import AST.ClassDecl;
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
import AST.SimpleClass;
import AST.TypeDecl;
import AST.EnumDecl;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.Multimap;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;

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
            System.out.println( "will add renamed enum: " + name );
            ptDeclToBeRewritten.getPTEnumDeclList().add( getRenamedEnumByName( name ) );
        }
    }

    /** Create interfaces 'inherited' from templates, with their new names.
     */

	protected void createRenamedInterfaces() {
        /* Interfaces can't have adds-classes, so it makes little sense
           to generalize the "create an empty adds-class and merge"
           for interfaces. Instead we just iterate through the
           instantiation tuples and rename as we go.
           Note, very very WIP. */
        for( String name : getDestinationIDsForInterfaces() ) {
            ptDeclToBeRewritten.getPTInterfaceDeclList().add( getRenamedInterfaceByName( name ) );
        }
    }

	/**
	 * Needs extended classes in correct order. Minit dependencies are inherited
	 * and therefore a superclass must be extended before its child.
	 */
	protected void extendAddClassesWithInstantiatons() {
		Set<String> visited = Sets.newHashSet();
		while (visited.size() < simpleClasses.size()) {
			for (SimpleClassRew decl : simpleClasses) {
				String superName = decl.getSuperClassname();
				if (!visited.contains(decl.getName())) {
					if (superName == null || visited.contains(superName)) {
						visited.add(decl.getName());
						decl.extendClass(getDestinationClassIDsWithInstTuples());
					}
				}
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

    /** Get a renamed interface by (new) name.
     */

    protected PTInterfaceDecl getRenamedInterfaceByName(String name) {
        // again, this just gets the first element. there should be only one.
        // xxx I saw a fancy google method for doing that somewhere
		Multimap<String, PTInstTuple> destinationClassIDsWithInstTuples = getDestinationClassIDsWithInstTuples();
        Set<String> rv = new TreeSet<String>();
        PTInstTuple tup = destinationClassIDsWithInstTuples.get(name).iterator().next();
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
		Set<String> missingAddsClass = Sets.difference(
                getDestinationIDsForNonEnumClasses(), addClasses );

		for (String name : missingAddsClass) {
			ClassDecl cls = new ClassDecl(new Modifiers(), name, new Opt<Access>(),
					new List<Access>(), new List<BodyDecl>());
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

	public void createInitIfPackage() {
		if (ptDeclToBeRewritten instanceof PTTemplate)
			return;
		String dummyName = addDummyClass();
		for (SimpleClassRew x : simpleClasses) {
			x.createInitConstructor(dummyName);
			x.createDummyConstructor(dummyName);
		}
	}

	private String addDummyClass() {
		String dummyName = "DUMMY$"; // TODO something better?
		ClassDecl dummy = new ClassDecl(new Modifiers(), dummyName, new Opt(),
				new List(), new List());
		ptDeclToBeRewritten.getSimpleClassList().add(new PTClassDecl(dummy));
		return dummyName;
	}
}
