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

public class JavaPrettyPrinter extends Frontend {

    public static void main(String args[]) throws java.io.IOException, beaver.Parser.Exception {
        if (!compile(args)) {
            System.exit(1);
        }
    }

    private static String srcString = "";
    private static String testName = "";

    public static boolean compileString(String testName, String srcString) {
        JavaPrettyPrinter.srcString = srcString;
        JavaPrettyPrinter.testName = testName;
        String[] args = {"test/PTExample.java"};
        return new JavaPrettyPrinter().process(
                args,
                new BytecodeParser(),
                new JavaParser() {
                    public CompilationUnit parse(java.io.InputStream is, String fileName) throws java.io.IOException, beaver.Parser.Exception {
                        InputStream is2 = new ByteArrayInputStream(JavaPrettyPrinter.srcString.getBytes("UTF-8"));
                        return new parser.JavaParser().parse(is2, JavaPrettyPrinter.testName);
                    }
                });
    }

    public static boolean compile(String args[]) throws java.io.IOException, beaver.Parser.Exception {
        System.out.println("JavaPrettyPrinter.java:");
        //return compileString("shouldFail", "template T { class A extends B { int a;}} package P { inst T with A=>X; class X adds { int x; }}");
        return new JavaPrettyPrinter().process(
                args,
                new BytecodeParser(),
                new JavaParser() {
                    public CompilationUnit parse(java.io.InputStream is, String fileName) throws java.io.IOException, beaver.Parser.Exception {
                        return new parser.JavaParser().parse(is, fileName);
                    }
                });
    }

    protected void processErrors(java.util.Collection errors, CompilationUnit unit) {
        //System.out.println(unit.dumpTreeNoRewrite());
        System.out.println(unit.toString());
        super.processErrors(errors, unit);
    }

    protected void processWarnings(java.util.Collection warnings, CompilationUnit unit) {
        super.processWarnings(warnings, unit);
    }

    protected void processNoErrors(CompilationUnit unit) {
        //System.out.println(unit.dumpTreeNoRewrite());
        System.out.println(unit.toString());
    }

    protected String name() {
        return "Java1.4Frontend + Backend + Java5Extensions Dumptree";
    }

    protected String version() {
        return "R20071015";
    }
}
