package javaworld;

import java.util.Map;

import AST.ClassDecl;
import AST.PTDummyClass;
import AST.PTDummyRename;

import com.google.common.collect.Maps;

public class InstTupleRew {

	final PTDummyClass instantiator;

	public InstTupleRew(PTDummyClass dummy) {
		this.instantiator = dummy;
	}

	protected ClassDeclRew getRenamedSourceClass() {
		ClassDecl ext = instantiator.getOriginator().fullCopy();
		ClassDeclRew x = new ClassDeclRew(ext, getSourceTemplateName());
		x.renameTypes(instantiator.getInstDecl().getRenamedClasses());
		x.degradeTSuperToAncestor();
		x.renameDefinitions(getExplicitlyRenamedDefinitions());
		x.renameConstructors(instantiator);
		return x;
	}

	Map<String, String> getExplicitlyRenamedDefinitions() {
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
