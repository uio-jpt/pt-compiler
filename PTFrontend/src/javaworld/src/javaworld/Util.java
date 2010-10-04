package javaworld;

import java.util.NoSuchElementException;

import AST.ClassDecl;
import AST.Expr;
import AST.List;
import AST.PTClassDecl;
import AST.TemplateConstructorAccess;
import AST.TemplateConstructorAccessShort;
import AST.TemplateMethodAccess;
import AST.TemplateMethodAccessShort;

import com.google.common.base.Preconditions;


public class Util {
	final static boolean debugMode = true;
 
	public static void print(String data) {
		if (debugMode)
			System.out.println("DM: " + data);
	}

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

	public static TemplateConstructorAccess rewriteConstructorAccess(
			TemplateConstructorAccessShort from) {
		String templateID = "";
		PTClassDecl host = (PTClassDecl) from.getParentClass(PTClassDecl.class);

		List<Expr> argList = from.getArgList(); // getArgListNoTransform??
		String tclassID = from.getTClassID();
		try {
			templateID = host.getClassDecl().lookupTemplateForTClass(tclassID);
		} catch (NoSuchElementException e) {
			from.error("Unknown template superclass: " + tclassID);
		} catch (IllegalArgumentException e) {
			from.error(String.format(
					"Multiple possible templates with class %s "
							+ "in template constructor call %s. msg: %s",
					tclassID, toName(tclassID), e.getMessage()));
		}
		String methodName = toMinitName(templateID,tclassID);
		return new TemplateConstructorAccess(methodName, argList, tclassID,
				templateID);
	}
	public static TemplateMethodAccess rewriteMethodAccess(TemplateMethodAccess from) {
		// we have the form tsuper[T.C].m(); ..
        // should we add some rewriting to support super calls?
		
		/* Possible extension:
		 * Check if the method exists in adds class:
		 * Search left to right..
		 * then upwards in each super class?
		 * 
		 * Current method:
		 * Never look in any super class.
		 * 
		 */
		/* ClassDecl cd = from.getClassDecl(from.getTClassID());
		System.out.println("Got classdecl: "+ from.getTClassID()+"=>" +cd);*/		
		return from;
	}

	
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
		return new TemplateMethodAccess(toName(templateID,tclassID, methodName), argList, tclassID,
				templateID);
	}
	
	public static ClassDecl toPTC(ClassDecl decl) {
		return decl;
	}

	public static String toAncestorName(String id, String sourceTemplateID,
			String id2) {
		return String.format("%s$%s_%s",id,sourceTemplateID,id2);

	}

	public static String toMinitName(String templateID, String tclassID) {
		return String.format("minit$%s$%s", templateID,tclassID);
	}
	
	public static String toMinitName(String tclassID) {
		return String.format("minit$%s",tclassID);
	}
}
