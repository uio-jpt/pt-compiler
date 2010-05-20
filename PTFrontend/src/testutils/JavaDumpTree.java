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

import java.util.*;
import java.io.*;

import testutils.javaparser.PTJavaParser;

public class JavaDumpTree extends PTFrontend {
  public static void main(String args[]) {
    compile(args);
  }

    public JavaDumpTree(String[] args, JavaParser parser) {
        super(args, parser);
    }
    

  public static boolean compile(String args[]) {
      try {
            JavaParser javaParser = new PTJavaParser();
            JavaDumpTree tester = new JavaDumpTree(args,javaParser);
            return tester.process();
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }

  }
}
