package javaworld;

import AST.PTClassDecl;

public class Util {
	final static boolean debugMode = true;
	
	public static void print(String data) {
		if (debugMode)
			System.out.println("DM: "+ data);
	}

	public static String toName(String templateName,
			String className) {
		return String.format("tsuper[%s.%s]",templateName,className);
	}
	
	public static String toName(String templateName,
			String className, String methodName) {
		return String.format("tsuper[%s.%s].%s",templateName,className,methodName);
	}
}	
