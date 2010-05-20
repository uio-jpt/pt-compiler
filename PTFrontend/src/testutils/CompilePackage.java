package testutils;

import AST.*;
import java.io.File;
import java.util.LinkedList;

public class CompilePackage extends Frontend {

    public static boolean compile(String[] args) {
        boolean res =  false;
        try {
            res = new Tester().PTProcess(
                args,
                new BytecodeParser(),
                new JavaParser() {
                    public CompilationUnit parse(java.io.InputStream is, String fileName) throws java.io.IOException, beaver.Parser.Exception {
                        CompilationUnit cu = new parser.JavaParser().parse(is, fileName);
                        System.out.println(cu.dumpTreeNoRewrite());
                        System.out.println(cu.toString());
                        return cu;
                    }
                });
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            return false;
        }
        return res;
    }

    protected void processErrors(java.util.Collection errors, CompilationUnit unit) {
        System.out.println(unit.dumpTreeNoRewrite());
        //System.out.println(unit.toString());
        super.processErrors(errors, unit);
    }

    protected void processWarnings(java.util.Collection warnings, CompilationUnit unit) {
        if (Tester.shouldBeOk || Tester.isSingle) super.processWarnings(warnings, unit);
    }

    protected void processNoErrors(CompilationUnit unit) {
        if (Tester.verbose) System.out.println(unit.dumpTreeNoRewrite());
        if (Tester.verbose) System.out.println(unit.toString());
    }

    /*Tester() {

    }*/

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
        String[] files = {"Sandbox/p1.jav"};

        if (!compile(files))
            System.out.println("feil med kompilering");
        
    }
}
