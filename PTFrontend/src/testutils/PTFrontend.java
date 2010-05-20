package testutils;
import AST.*;

import java.util.*;
import java.io.File;

import testutils.javaparser.PTJavaParser;

public class PTFrontend {
	
    public static boolean compile(String[] args) {
	    try {
	       
	        JavaParser javaParser = new PTJavaParser();
	        Tester tester = new Tester(args,javaParser);
	        return tester.process();
	    }
	    catch (Exception e) {
	        e.printStackTrace();
	        return false;
	    }
	}
    
    public static boolean compile(String filename) {
    	String[] filenames = {filename};
    	return compile(filenames);
    }

	protected Program program;
    Collection files;

    public PTFrontend() {
        program = new Program();
        program.state().reset();
    }

    public PTFrontend(String[] args, BytecodeReader reader, JavaParser parser) {
        this();
        init(args,reader,parser);
    }

    public PTFrontend(String[] args,JavaParser p) {
        this();
        init(args,p);
    }
    
    public void init(String[] args, JavaParser parser) {
        BytecodeReader reader = new BytecodeParser();
        init(args,reader,parser);
    }

    public void init(String[] args, BytecodeReader reader, JavaParser parser) {
        program.initBytecodeReader(reader);
        program.initJavaParser(parser);
        initOptions();
        processArgs(args);
        files = program.options().files();

        if(program.options().hasOption("-version")) {
            printVersion();
        } else if (program.options().hasOption("-help") || files.isEmpty()) {
            printUsage();
        }
    }

    public boolean process() {
        try {
            for(Iterator iter = files.iterator(); iter.hasNext(); ) {
                String name = (String)iter.next();
                if(!new File(name).exists())
                    System.out.println("WARNING: file \"" + name + "\" does not exist");
                program.addSourceFile(name);
            }

            for(Iterator iter = program.compilationUnitIterator(); iter.hasNext(); ) {
                CompilationUnit unit = (CompilationUnit)iter.next();
                if(unit.fromSource()) {
                    Collection errors = unit.parseErrors();
                    Collection warnings = new LinkedList();
                    // compute static semantic errors when there are no parse errors or 
                    // the recover from parse errors option is specified
                    if(errors.isEmpty() || program.options().hasOption("-recover"))
                        unit.errorCheck(errors, warnings);
                    if(!errors.isEmpty()) {
                        processErrors(errors, unit);
                        return false;
                    }
                    else {
                        processWarnings(warnings, unit);
                        processNoErrors(unit);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
            return false;
        }
        return true;
    }

    protected void initOptions() {
        Options options = program.options();
        options.initOptions();
        options.addKeyOption("-version");
        options.addKeyOption("-print");
        options.addKeyOption("-g");
        options.addKeyOption("-g:none");
        options.addKeyOption("-g:lines,vars,source");
        options.addKeyOption("-nowarn");
        options.addKeyOption("-verbose");
        options.addKeyOption("-deprecation");
        options.addKeyValueOption("-classpath");
        options.addKeyValueOption("-sourcepath");
        options.addKeyValueOption("-bootclasspath");
        options.addKeyValueOption("-extdirs");
        options.addKeyValueOption("-d");
        options.addKeyValueOption("-encoding");
        options.addKeyValueOption("-source");
        options.addKeyValueOption("-target");
        options.addKeyOption("-help");
        options.addKeyOption("-O");
        options.addKeyOption("-J-Xmx128M");
        options.addKeyOption("-recover");
    }
    protected void processArgs(String[] args) {
        program.options().addOptions(args);
    }

    protected void processErrors(Collection errors, CompilationUnit unit) {
        System.out.println("Errors:");
        for(Iterator iter2 = errors.iterator(); iter2.hasNext(); ) {
            System.out.println(iter2.next());
        }
    }
    protected void processWarnings(Collection warnings, CompilationUnit unit) {
        for(Iterator iter2 = warnings.iterator(); iter2.hasNext(); ) {
            System.out.println(iter2.next());
        }
    }
   

    protected void printUsage() {
        printVersion();
        System.out.println(
                           "\n" + name() + "\n\n" +
                           "Usage: java " + name() + " <options> <source files>\n" +
                           "  -verbose                  Output messages about what the compiler is doing\n" +
                           "  -classpath <path>         Specify where to find user class files\n" +
                           "  -sourcepath <path>        Specify where to find input source files\n" + 
                           "  -bootclasspath <path>     Override location of bootstrap class files\n" + 
                           "  -extdirs <dirs>           Override location of installed extensions\n" +
                           "  -d <directory>            Specify where to place generated class files\n" +
                           "  -help                     Print a synopsis of standard options\n" +
                           "  -version                  Print version information\n"
                           );
    }

    protected void printVersion() {
        System.out.println(name() + " " + url() + " Version " + version());
    }

    protected String name() {
        return "Java1.4PTFrontend";
    }
    protected String url() {
        return "(http://jastadd.cs.lth.se)";
    }

    protected String version() {
        return "R20070504";
    }

    public boolean PTProcess(String[] args, BytecodeReader reader, JavaParser parser) {
        program.initBytecodeReader(reader);
        program.initJavaParser(parser);

        initOptions();
        processArgs(args);

        Collection files = program.options().files();

        if(program.options().hasOption("-version")) {
            printVersion();
            return false;
        }
        if(program.options().hasOption("-help") || files.isEmpty()) {
            printUsage();
            return false;
        }

        try {
            for(Iterator iter = files.iterator(); iter.hasNext(); ) {
                String name = (String)iter.next();
                if(!new File(name).exists())
                    System.out.println("WARNING: file \"" + name + "\" does not exist");
                program.addSourceFile(name);
            }

            for(Iterator iter = program.compilationUnitIterator(); iter.hasNext(); ) {
                CompilationUnit unit = (CompilationUnit)iter.next();
                if(unit.fromSource()) {
                    Collection errors = unit.parseErrors();
                    Collection warnings = new LinkedList();
                    // compute static semantic errors when there are no parse errors or 
                    // the recover from parse errors option is specified
                    if(errors.isEmpty() || program.options().hasOption("-recover"))
                        unit.errorCheck(errors, warnings);
                    if(!errors.isEmpty()) {
                        processErrors(errors, unit);
                        return false;
                    }
                    else {
                        processWarnings(warnings, unit);
                        processNoErrors(unit);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
            return false;
        }
        return true;
    }

	

	

	protected void processNoErrors(CompilationUnit unit) {
	    System.out.println(unit.dumpTreeNoRewrite());
	    System.out.println(unit.toString());
	}
}

