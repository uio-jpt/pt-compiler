/*
 * The JastAdd Extensible Java Compiler (http://jastadd.org) is covered
 * by the modified BSD License. You should have received a copy of the
 * modified BSD license with this compiler.
 * 
 * Copyright (c) 2005-2008, Torbjorn Ekman
 * All rights reserved.
 */
package testutils;
import testutils.javaparser.PTJavaParser;
import AST.*;

public class JavaChecker extends PTFrontend {
  public static void main(String args[]) {
    compile(args);
  }

    public JavaChecker(String[] args, JavaParser parser) {
        super(args, parser);
    }

  protected String name() { return "Java1.4Frontend + Java1.5Extensions"; }
  protected String version() { return "R20071015"; }
}
