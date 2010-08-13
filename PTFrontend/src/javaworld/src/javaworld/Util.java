package javaworld;

import java.util.Collection;
import java.util.NoSuchElementException;

import AST.Access;
import AST.Block;
import AST.ClassDecl;
import AST.ConstructorDecl;
import AST.Expr;
import AST.ExprStmt;
import AST.List;
import AST.Modifiers;
import AST.Opt;
import AST.PTClassDecl;
import AST.ParameterDeclaration;
import AST.Stmt;
import AST.TemplateConstructor;
import AST.TemplateConstructorAccess;
import AST.TemplateConstructorAccessShort;
import AST.TemplateMethodAccess;
import AST.TemplateMethodAccessShort;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

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
		print("rewriting tconstructor access with short name " + from.getID());
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
		String methodName = toName(templateID,tclassID);
		return new TemplateConstructorAccess(methodName, argList, tclassID,
				templateID);
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
		return new TemplateMethodAccess(methodName, argList, tclassID,
				templateID);
	}
	
	public static ClassDecl toPTC(ClassDecl decl) {
		return decl;
	}
}
