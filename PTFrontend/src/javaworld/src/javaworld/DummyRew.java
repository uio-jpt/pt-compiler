package javaworld;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import AST.ClassDecl;
import AST.PTDummyClass;
import AST.PTDummyRename;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class DummyRew {

	final PTDummyClass instantiator;

	public DummyRew(PTDummyClass dummy) {
		this.instantiator = dummy;
	}

	public ClassDeclRew getRenamedSourceClass() {
		ClassDecl ext = instantiator.getOriginator().fullCopy();
		ClassDeclRew x = new ClassDeclRew(ext, getSourceTemplateName());
		x.renameTypes(instantiator.getInstDecl().getRenamedClasses());
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

	/**
	 * Returns templatename of instantiated template with templateName and
	 * methodname. TODO Will fail when two templates have the same Classname and
	 * methodname but possibly different signatures... (not consistent)
	 */
	public boolean sourceClassHasNameAndMethod(String templateClassname,
			String methodName) {
		/*
		String origID = instantiator.getOrgID();
		Set<String> methodNames = Sets.newHashSet();
		for (String x : getDefinitionsRenamed()) {
			// removes all signatures
			x = x.split("\\(")[0];
			methodNames.add(x);
		}
		return origID.equals(templateClassname)
				&& methodNames.contains(methodName);
				*/
		throw new NotImplementedException();
	}

	private String getSourceTemplateName() {
		return instantiator.getTemplate().getID();
	}

}
