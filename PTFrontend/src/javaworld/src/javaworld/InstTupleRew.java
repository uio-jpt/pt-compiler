package javaworld;

import java.util.Map;

import AST.ClassDecl;
import AST.PTInstTuple;
import AST.PTDummyRename;

import com.google.common.collect.Maps;

public class InstTupleRew {

	final PTInstTuple instantiator;

	public InstTupleRew(PTInstTuple dummy) {
		this.instantiator = dummy;
	}

	protected ClassDeclRew getRenamedSourceClass() {
		ClassDecl ext = instantiator.getOriginator().fullCopy();
		ClassDeclRew rewriteClass = new ClassDeclRew(ext, getSourceTemplateName());
		rewriteClass.renameConstructors(instantiator);
		rewriteClass.renameTypes(instantiator.getInstDecl().getRenamedClasses());
		// TODO still relevant?
//		x.degradeTSuperToAncestor();
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
