package javaworld;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import AST.ClassDecl;
import AST.Expr;
import AST.PTDummyClass;
import AST.PTDummyRename;

import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class DummyRew {

	final PTDummyClass instantiator;
	private final ClassDeclRew renamedClass;
	private Map<String, String> renamedDefs;

	public DummyRew(PTDummyClass dummy) {
		this.instantiator = dummy;
		renamedClass = getRenamedSourceClass();
		renamedDefs = getExplicitlyRenamedDefinitions();
	}

	Set<String> getDefinitionsRenamed() {
		ClassDeclRew cls = new ClassDeclRew(instantiator.getOriginator());
		return cls.getDefinitionsRenamed(renamedDefs);

	}

	Map<String, String> getRenamedConflictsMap(Set<String> conflicts) {
		HashMap<String, String> newDefinitions = new HashMap<String, String>();
		for (String conflictDef : conflicts) {
			String origClassName = instantiator.getOrgID();
			newDefinitions.put(conflictDef, String.format("super[%s.%s].%s",
					instantiator.getTemplate().getID(), origClassName,
					conflictDef));
		}
		return newDefinitions;
	}

	Map<String, String> getExplicitlyRenamedDefinitions() {
		HashMap<String, String> map = Maps.newHashMap();
		for (PTDummyRename entry : instantiator.getPTDummyRenameList()) {
			entry.addSelfTo(map);
		}
		return map;
	}

	public ClassDeclRew getRenamedSourceClass() {
		ClassDecl ext = instantiator.getOriginator().fullCopy();
		ClassDeclRew x = new ClassDeclRew(ext);
		x.renameTypes(instantiator.getInstDecl().getRenamedClasses());
		x.renameDefinitions(getExplicitlyRenamedDefinitions());
		return x;
	}

	/**
	 * Returns templatename of instantiated template with templateName and
	 * methodname. TODO Will fail when two templates have the same Classname and
	 * methodname but possibly different signatures... (not consistent)
	 */
	public boolean sourceClassHasNameAndMethod(String templateClassname,
			String methodName) {
		String origID = instantiator.getOrgID();
		Set<String> methodNames = Sets.newHashSet();
		for (String x : getDefinitionsRenamed()) {
			// removes all signatures
			x = x.split("\\(")[0];
			methodNames.add(x);
		}
		return origID.equals(templateClassname)
				&& methodNames.contains(methodName);
	}

}
