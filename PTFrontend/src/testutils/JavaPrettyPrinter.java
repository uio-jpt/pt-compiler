/*
 * The JastAdd Extensible Java Compiler (http://jastadd.org) is covered
 * by the modified BSD License. You should have received a copy of the
 * modified BSD license with this compiler.
 * 
 * Copyright (c) 2005-2008, Torbjorn Ekman
 * All rights reserved.
 */
package testutils;

import AST.*;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

import testutils.javaparser.PTJavaParser;

public class JavaPrettyPrinter extends PTFrontend {

    public static void main(String args[]) throws java.io.IOException, beaver.Parser.Exception {
        if (!compile(args)) {
            System.exit(1);
        }
    }

    private static String srcString = "";
    private static String testName = "";

    public JavaPrettyPrinter(String[] args, JavaParser parser) {
        super(args, parser);
    }
    

  public static boolean compileString(String testName, String srcString) {
        JavaPrettyPrinter.srcString = srcString;
        JavaPrettyPrinter.testName = testName;
        String[] args = {testName,srcString};
      try {
            JavaParser javaParser = new PTJavaParser();
            JavaPrettyPrinter tester = new JavaPrettyPrinter(args,javaParser);
            return tester.process();
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }

  }
    protected String name() {
        return "Java1.4Frontend + Backend + Java5Extensions Dumptree";
    }

    protected String version() {
        return "R20071015";
    }
}
