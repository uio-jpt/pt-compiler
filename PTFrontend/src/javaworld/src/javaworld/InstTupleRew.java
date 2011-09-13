package javaworld;

import java.util.Map;

import AST.TypeDecl;
import AST.ClassDecl;
import AST.PTInstTuple;
import AST.PTDummyRename;
import AST.PTInterfaceDecl;
import AST.PTEnumDecl;
import AST.BodyDecl;
import AST.ASTNode;
import AST.PTMethodRename;
import AST.PTMethodRenameAll;
import AST.PTFieldRename;
import AST.Access;
import AST.MethodDecl;
import AST.SimpleSet;


import java.util.Iterator;
import java.util.HashMap;

import com.google.common.collect.Maps;
import com.google.common.base.Joiner;

class InstTupleRew {

	private final PTInstTuple instantiator;

	public InstTupleRew(PTInstTuple dummy) {
		this.instantiator = dummy;
	}

    protected boolean isInterface() {
        return instantiator.getOriginator() instanceof AST.PTInterfaceDecl;
    }

    protected static TypeDecl lookupUnambiguousTypeIn( TypeDecl root, String name ) {
        SimpleSet matches = root.lookupType( name );
        if( matches.size() != 1 ) {
            throw new RuntimeException( "unexpectedly unable to find type -- " + name );
        }
        return (TypeDecl) matches.iterator().next();
    }

    protected HashMap<ASTNode,String> findInternalRenames(TypeDecl target) {
        // XXX what about types that can only be found in the parent, are these renamed correctly?
        // do any such exist? is the parent always ptdecl?

        HashMap< ASTNode, String > rv = new HashMap< ASTNode, String >();

        for( PTDummyRename ptdr : instantiator.getPTDummyRenameList() ) {
            String originalId = ptdr.getOrgID();
            String destinationId = ptdr.getID();

            if( ptdr instanceof PTFieldRename ) {
                // TODO
            } else if( ptdr instanceof PTMethodRename ) {
                PTMethodRename ptmr = (PTMethodRename) ptdr;
                AST.List<Access> args = ptmr.getAccessList();
                boolean foundMethod = false;
                for( Object declo : target.memberMethods( originalId ) ) {
                    MethodDecl mdecl = (MethodDecl) declo;
                    if( mdecl.arity() != args.getNumChild() ) continue;
                    boolean ok = true;
                    for(int i=0;i<mdecl.arity();i++) {
                        TypeDecl formalParamTypeDecl = mdecl.getParameter(i).type();
                        TypeDecl fPTDinCopy = lookupUnambiguousTypeIn( target, formalParamTypeDecl.fullName() );
                        Access myAcc = args.getChild(i);

                        if( myAcc.type() != fPTDinCopy ) {
                            ok = false;
                            continue;
                        }
                    }

                    if( ok ) {
                        foundMethod = true;
                        rv.put( mdecl, destinationId );
                    }
                }

                if( !foundMethod ) {
                    ptmr.error( "cannot find method matching rename: " + ptmr );
                }
            } else if( ptdr instanceof PTMethodRenameAll ) {
                for( Object declo : target.memberMethods( originalId ) ) {
                    ASTNode declNode = (ASTNode) declo;
                    rv.put( declNode, destinationId );
                }
            } else {
                throw new RuntimeException( "program error -- unexpected PTDummyRename" );
            }
        }

        return rv;
    }

    protected PTInterfaceDecl getRenamedSourceInterface() {
        TypeDecl x = instantiator.getOriginator();

		PTInterfaceDecl ext = ((PTInterfaceDecl)x).fullCopy();

        HashMap<ASTNode,String> internalRenames = findInternalRenames( ext );

        ext.visitRenameAccesses( internalRenames );
        ext.visitRenameDeclarations( internalRenames );

//        ext.renameMethods()

            // is this a wise way to do this? seems clumsy.
            // renameTypes should evidently NOT automatically visitRename
            //  as well, this breaks several tests -- should investigate why
        ext.visitRename( instantiator.getInstDecl().getRenamedClasses() );
        ext.renameTypes( instantiator.getInstDecl().getRenamedClasses() );

        ext.flushCaches();

        return ext;
    }

    protected PTEnumDecl getRenamedSourceEnum() {
        // straight rewrite of getRenamedSourceInterface, above concerns apply

        TypeDecl x = instantiator.getOriginator();
		PTEnumDecl ext = ((PTEnumDecl)x).fullCopy();

        ext.fixupAfterCopy();

            // do we need both?
        ext.visitRename( instantiator.getInstDecl().getRenamedClasses() );
        ext.renameTypes( instantiator.getInstDecl().getRenamedClasses() );

        ext.fixupAfterCopy();

        return ext;
    }

	protected ClassDeclRew getRenamedSourceClass() {
        TypeDecl x = instantiator.getOriginator();

		ClassDecl ext = ((ClassDecl)x).fullCopy();
            /* problem:
                the copy is shallow -- it contains references to types in the original.
                these are not == to the corresponding ones in the copy.
                so e.g.:

                class A {
                    int getFoo(A x) { return 42; }
                }
                    ->
                class *copy*A {
                    int getFoo( *original*A ) { return 42; }
                }
            */

		ClassDeclRew rewriteClass = new ClassDeclRew(ext, getSourceTemplateName());

        HashMap<ASTNode,String> internalRenames = findInternalRenames( ext );

        ext.visitRenameAccesses( internalRenames );
        ext.visitRenameDeclarations( internalRenames );

		rewriteClass.renameConstructors(instantiator);
		rewriteClass.renameTypes(instantiator.getInstDecl().getRenamedClasses());
		rewriteClass.renameDefinitions(getExplicitlyRenamedDefinitions());
		return rewriteClass;
	}

    /* This seems to be based on reducing the signature to a string, which
       seems to me like it might be impossible because of e.g.:

            class V {
                class B {
                    class X {
                    }
                }
                class I {
                    class X {
                    }
                }
            }

        As such, deprecated. TODO replace all references to this system.
    */
	private Map<String, String> getExplicitlyRenamedDefinitions() {
		// TODO addsselfto... move it here!!!
		Map<String, String> map = Maps.newHashMap();
		for (PTDummyRename entry : instantiator.getPTDummyRenameList()) {
			entry.addSelfTo(map);
		}
		return map;
	}

	private String getSourceTemplateName() {
		return instantiator.getTemplate().getID();
	}

}
