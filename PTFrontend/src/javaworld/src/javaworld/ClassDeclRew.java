package javaworld;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

import AST.BodyDecl;
import AST.ClassDecl;
import AST.ConstructorDecl;
import AST.FieldDeclaration;
import AST.List;
import AST.MethodAccess;
import AST.MethodDecl;
import AST.PTInstTuple;
import AST.SimpleSet;
import AST.VarAccess;
import AST.TypeAccess;
import AST.Access;
import AST.PTDecl;
import AST.SimpleClass;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import com.google.common.collect.Maps;

class ClassDeclRew {
	protected final ClassDecl ext;
	private final String sourceTemplateID;

    public PTDecl getContainingPTDecl() {
        return (PTDecl) ext.getParentClass( PTDecl.class );
    }

    public SimpleClass getContainingSimpleClass() {
        // this juggling of simpleclasses is a mess, I'm starting
        // to be convinced the alternative without them is better
        return (SimpleClass) ext.getParentClass( SimpleClass.class );
    }

    public ClassDecl getClassDecl() {
        return ext;
    }

	ClassDeclRew(ClassDecl ext, String sourceTemplateID) {
		this.sourceTemplateID = sourceTemplateID;
		Preconditions.checkArgument(sourceTemplateID != null);
		this.ext = ext;

	}

    /*
        Deprecated: implements-list cannot be fully expressed as a set of strings,
                    must use accesses.

        Replace with simply .getImplementsList() in ClassDecl, corresponding
        to .setImplementsList(). These operate on Accesses.
    */

    public HashSet<String> getImplementsList() {
        HashSet<String> rv = new HashSet<String> ();
        for( Access x : ext.getImplementsList() ) {
            TypeAccess iface = (TypeAccess) x ;
            rv.add( iface.getID() );
        }
        return rv;
    }


	/* TODO not very pretty 
	 * 
	 * Renames a constructor to a method with name minit$<<TemplateName>>$<<OriginalClassName>>
	 * Adds a super method call with the same name style if needed.
	 * 
	 */
	protected void renameConstructors(PTInstTuple instantiator) {
		int i = -1;
		for (BodyDecl decl : ext.getBodyDeclList()) {
			i++;
			if (decl instanceof ConstructorDecl) {
				ConstructorDecl cd = (ConstructorDecl) decl;
				ConstructorRew cdRew = new ConstructorRew(cd, sourceTemplateID,
						instantiator.getOrgID());
				try {
					decl = cdRew.toMethodDecl();
					ext.setBodyDecl(decl, i);

				} catch (Exception e) {
                    e.printStackTrace();
					cd.error("Could not rewrite constructor " + cd.dumpString()
							+ " to method during class merging.\n");
				}
			}
		}
	}

    /* A general way to say, e.g. ".renameTypes". */

    protected void applyMutator(NodeMutator mutator) {
        mutator.mutate( ext );
    }

	protected void renameTypes(Map<String, String> renamedClasses) {
		ext.renameTypes(renamedClasses);
	}

	protected Set<String> getSignatures() {
		Builder<String> x = ImmutableSet.builder();
		x.addAll(ext.methodSignatures());
		x.addAll(ext.fieldNames());
		return x.build();
	}

	protected List<BodyDecl> getBodyDecls() {
		return ext.getBodyDecls();
	}

	protected void renameMatchingMethods(Set<String> conflicts) {
		final String templateName = sourceTemplateID;
		final String className = ext.getID();
		Map<String, String> renamedVersion = Maps.newHashMap();
		for (String memberNameToBeRenamed : conflicts) {
			String tsuperName = Util.toName(templateName, className,
					memberNameToBeRenamed);
			renamedVersion.put(memberNameToBeRenamed, tsuperName);
		}
		renameDefinitions(renamedVersion);
	}

    protected boolean isAbstract() {
        return ext.isAbstract();
    }

    protected HashSet getImplementedInterfaces() {
        return ext.implementedInterfaces();
    }

	protected String getSuperClassName() {
		return ext.getSuperClassName();
	}

	/* 
	 * Renames methods or fields which have renaming explicitly
	 * given in the inst clauses.
	 * TODO make pretty
	 */
	void renameDefinitions(Map<String, String> namesMap) {
        DefinitionsRenamer.renameDefinitions( ext, namesMap );
        /*
		Map<String, MethodDecl> methods = ext.methodsSignatureMap();
		Map<String, SimpleSet> fields = ext.memberFieldsMap();
        
        if( namesMap.isEmpty() ) {
            return;
        }

        if( ext.inheritsFromExtendsExternal() ) {
            ext.error( "cannot rename definitions in " + ext.getID() + " which inherits from external" );
            return;
        }

        Set<String> namesToRename = new HashSet<String>();
        for( String key : namesMap.keySet() ) {
            System.out.println( "expecting to rename: " + key );
            namesToRename.add( key );
        }
        System.out.println( "expecting in this: " + ext );

		for (MethodDecl decl : methods.values()) {
                * If we rename the tabstracts we have trouble recognizing
                   their signatures later. More elegant way? *
            if( decl.isTabstract() ) continue; // XXX HAX

			if (namesMap.containsKey(decl.signature())) {
				String newID = namesMap.get(decl.signature());
				newID = newID.split("\\(")[0];
				for (MethodAccess x : decl.methodAccess()) { // <-- note, very handy JaJ method
					x.setID(newID);
                }

                String oldSig = decl.signature();

				decl.setID(newID);

                if( namesToRename.contains( oldSig ) ) {
                    namesToRename.remove( oldSig );
                }
			}
		}

		for (SimpleSet simpleSet : fields.values()) {
			for (Iterator iter = simpleSet.iterator(); iter.hasNext();) {
				FieldDeclaration fieldDecl = (FieldDeclaration) iter.next();
				if (namesMap.containsKey(fieldDecl.getID())) {
					String newID = namesMap.get(fieldDecl.getID());
					for (VarAccess x : fieldDecl.fieldAccess()) { // <-- similarly, very handy JaJ method
						x.setID(newID);
                    }
                    String oldId = fieldDecl.getID();

					fieldDecl.setID(newID);
                    System.out.println( "renamed: " + oldId + " -> " + newID );

                    if( namesToRename.contains( oldId ) ) {
                        namesToRename.remove( oldId );
                    }
				}
			}
		}
        */
	}

	@Override
	public String toString() {
		return ext.toString();
	}
}
