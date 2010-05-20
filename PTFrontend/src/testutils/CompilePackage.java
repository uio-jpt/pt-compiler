package testutils;

import AST.*;

import java.io.File;
import java.util.LinkedList;

import testutils.javaparser.PTJavaParser;

public class CompilePackage extends PTFrontend {

    public CompilePackage(String[] args, JavaParser parser) {
		super(args, parser);
    }
    
    public static boolean verbose = false;
    public static boolean stopFirst = false;
    public static boolean isSingle = false;
    public static int total = 0;
    public static int totalFail = 0;
    public static int totalOK = 0;
    public static boolean shouldBeOk;
    public static LinkedList<String> notPassed = new LinkedList<String>();

    public static void main(String[] args) {
        for (String f : args) {
            if (f.equals("--stopfirst")) {
                stopFirst = true;
                continue;
            }
            if (f.contains("--verbose")) {
                verbose = true;
                continue;
            }
        }
        String[] files = {"test/multiple_files/simple/SimpleT.ptjava",
                          "test/multiple_files/simple/SimpleP.ptjava"};

        if (!compile(files))
            System.out.println("feil med kompilering");
        
    }
}
