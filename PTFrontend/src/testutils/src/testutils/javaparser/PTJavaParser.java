package testutils.javaparser;

import AST.*;
import java.io.*;

public class PTJavaParser implements JavaParser {
    public CompilationUnit parse(InputStream is, String fileName)
        throws IOException, beaver.Parser.Exception {
    	// parser referer til pakken parser
        return new parser.JavaParser().parse(is, fileName);
    }
}

/** Ivar testfil 
 * ikke i bruk atm, tror kanskje vi kan unngå denne.
 * */
class CompilePackageJavaParser implements JavaParser {
    public CompilationUnit parse(InputStream is, String fileName)
        throws IOException, beaver.Parser.Exception {
        CompilationUnit cu = new parser.JavaParser().parse(is, fileName);
        System.out.println(cu.dumpTreeNoRewrite());
        System.out.println(cu.toString());
        return cu;
    }
}
