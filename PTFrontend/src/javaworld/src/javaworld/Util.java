package javaworld;

import java.util.NoSuchElementException;

import AST.Expr;
import AST.List;
import AST.PTClassDecl;
import AST.TemplateMethodAccess;
import AST.TemplateMethodAccessShort;

import com.google.common.base.Preconditions;

public class Util {
	final static boolean debugMode = true;

	public static String toName(String id) {
		Preconditions.checkArgument(id != null);
		return String.format("tsuper[%s]", id);
	}

	public static String toName(String templateName, String className) {
		Preconditions.checkArgument(templateName != null && className != null);
		return toName(templateName + "." + className);
	}

	public static String toName(String templateName, String className,
			String methodName) {
		Preconditions.checkArgument(methodName != null);
		return String.format("%s.%s", toName(templateName, className),
				methodName);
	}

	public static String getName(String methodSignature) {
		int splitIdx = methodSignature.indexOf('(');
		return methodSignature.substring(0, splitIdx);
	}

	/*
	 * Is called from InstantiationRewrite.jrag
	 * Try to unambiguously resolve a tsuper call in short from to standard form.
	 * e.g. tsuper[<ClassID>].f() --> tsuper[<TemplateID>.<ClassID>].f()
	 */
	public static TemplateMethodAccess rewriteMethodAccess(
			TemplateMethodAccessShort from) {
		String templateID = "";
		PTClassDecl host = (PTClassDecl) from.getParentClass(PTClassDecl.class);
		String methodName = from.getID();
		List<Expr> argList = from.getArgList(); // getArgListNoTransform??
		String tclassID = from.getTClassID();
		try {
			templateID = host.getClassDecl().lookupTemplateForTClass(tclassID);
		} catch (NoSuchElementException e) {
			from.error("Unknown template superclass for " + tclassID);
		} catch (IllegalArgumentException e) {
			from.error(String.format(
					"Multiple possible templates with class %s "
							+ "in template method call %s. msg: ", tclassID,
					from, e.getMessage()));
		}
		return new TemplateMethodAccess(
				toName(templateID, tclassID, methodName), argList, tclassID,
				templateID);
	}


	public static String toMinitName(String templateID, String tclassID) {
		return String.format("minit$%s$%s", templateID, tclassID);
	}

	public static String toMinitName(String tclassID) {
		return String.format("minit$%s", tclassID);
	}
}
