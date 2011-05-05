package javaworld;

import java.util.Map;

import AST.TypeDecl;
import AST.ClassDecl;
import AST.PTInstTuple;
import AST.PTDummyRename;
import AST.PTInterfaceDecl;
import AST.PTEnumDecl;
import AST.BodyDecl;

import com.google.common.collect.Maps;

class InstTupleRew {

	private final PTInstTuple instantiator;

	public InstTupleRew(PTInstTuple dummy) {
		this.instantiator = dummy;
	}

    protected boolean isInterface() {
        return instantiator.getOriginator() instanceof AST.PTInterfaceDecl;
    }

    protected PTInterfaceDecl getRenamedSourceInterface() {
        TypeDecl x = instantiator.getOriginator();
		PTInterfaceDecl ext = ((PTInterfaceDecl)x).fullCopy();

            // is this a wise way to do this? seems clumsy.
            // renameTypes should evidently NOT automatically visitRename
            //  as well, this breaks several tests -- should investigate why
        ext.visitRename( instantiator.getInstDecl().getRenamedClasses() );
        ext.renameTypes( instantiator.getInstDecl().getRenamedClasses() );

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
		ClassDeclRew rewriteClass = new ClassDeclRew(ext, getSourceTemplateName());
		rewriteClass.renameConstructors(instantiator);
		rewriteClass.renameTypes(instantiator.getInstDecl().getRenamedClasses());
		rewriteClass.renameDefinitions(getExplicitlyRenamedDefinitions());
		return rewriteClass;
	}

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
