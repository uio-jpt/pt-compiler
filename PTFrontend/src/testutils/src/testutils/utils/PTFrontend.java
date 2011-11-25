package testutils.utils;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;

import testutils.tester.Log;
import AST.BytecodeParser;
import AST.BytecodeReader;
import AST.CompilationUnit;
import AST.JavaParser;
import AST.Options;
import AST.Problem;
import AST.Program;
import errorcheck.ErrorCheckBeforeRewrite;

public class PTFrontend {

	protected Program program;
	Collection files;
	private StringBuffer normalMsgs;
	private StringBuffer errorMsgs;
	private StringBuffer warningMsgs;

	public static Collection<File> getFilesInFolderAndSubFolders(File file,
			String extension) {
		String[] p = { extension };
		return FileUtils.listFiles(file, p, true);
	}

	protected static File[] getFileNamesInFolder(String inputFolder,
			String extension) {
		File folder = new File(inputFolder);
		if (folder.isDirectory()) {
			return folder.listFiles((FilenameFilter) new SuffixFileFilter(
					extension));
		} else {
			throw new IllegalArgumentException(inputFolder
					+ " is not a directory.");
		}
	}

	public PTFrontend() {
		program = new Program();
		program.state().reset();
	}

	public PTFrontend(String[] args, BytecodeReader reader, JavaParser parser) {
		this();
		init(args, reader, parser);
	}

	public PTFrontend(String[] args, JavaParser p) {
		this();
		init(args, p);
	}

	public void init(String[] args, JavaParser parser) {
		BytecodeReader reader = new BytecodeParser();
		init(args, reader, parser);
	}

	public void init(String[] args, BytecodeReader reader, JavaParser parser) {
		program.initBytecodeReader(reader);
		program.initJavaParser(parser);
		warningMsgs = new StringBuffer();
		errorMsgs = new StringBuffer();
		normalMsgs = new StringBuffer();
		initOptions();
		processArgs(args);
		files = program.options().files();

		if (program.options().hasOption("-version")) {
			printVersion();
		} else if (program.options().hasOption("-help") || files.isEmpty()) {
			printUsage();
		}
	}

	public boolean process() {
		try {
			for (Iterator iter = files.iterator(); iter.hasNext();) {
				String name = (String) iter.next();
				if (!new File(name).exists())
					System.out.println("WARNING: file \"" + name
							+ "\" does not exist");
                try {
                    program.addSourceFile(name);
                }
                catch( Error expectedUnexpectedError ) {
                    // Jastadd is throwing an Error here when our code crashes during compile.
                    // wrapping it allows us to catch something narrower in TestRunner
                    // (note that we could also crash during errorCheck, but that should be
                    //  a normal Exception)
                    throw new Exception( expectedUnexpectedError );
                }
			}

			// TODO: I think there's a bug somewhere in errorcheck.
			// for(Iterator iter = program.compilationUnitIterator();
			// iter.hasNext(); ) {
			// CompilationUnit unit = (CompilationUnit)iter.next();
			// ErrorCheckBeforeRewrite checkerr = new ErrorCheckBeforeRewrite();
			// checkerr.checkErrors(unit);
			// }

			for (Iterator iter = program.compilationUnitIterator(); iter
					.hasNext();) {
				CompilationUnit unit = (CompilationUnit) iter.next();

				if (unit.fromSource()) {
					Collection errors = unit.parseErrors();
					Collection warnings = new LinkedList();
					// compute static semantic errors when there are no parse
					// errors or
					// the recover from parse errors option is specified
					if (errors.isEmpty()
							|| program.options().hasOption("-recover")) {
						unit.errorCheck(errors, warnings);
					}
					if (!errors.isEmpty()) {
						processErrors(errors, unit);
						return false;
					} else {
						processWarnings(warnings, unit);
						processNoErrors(unit);
					}
				}
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
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

	public String getErrorMsgs() {
		return errorMsgs.toString();
	}

	public String getWarningMsgs() {
		return warningMsgs.toString();
	}

	public String getNormalMsgs() {
		return normalMsgs.toString();
	}

	protected void printUsage() {
		printVersion();
		System.out
				.println("\n"
						+ name()
						+ "\n\n"
						+ "Usage: java "
						+ name()
						+ " <options> <source files>\n"
						+ "  -verbose                  Output messages about what the compiler is doing\n"
						+ "  -classpath <path>         Specify where to find user class files\n"
						+ "  -sourcepath <path>        Specify where to find input source files\n"
						+ "  -bootclasspath <path>     Override location of bootstrap class files\n"
						+ "  -extdirs <dirs>           Override location of installed extensions\n"
						+ "  -d <directory>            Specify where to place generated class files\n"
						+ "  -help                     Print a synopsis of standard options\n"
						+ "  -version                  Print version information\n");
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

	protected void processErrors(Collection c, CompilationUnit unit) {
		errorMsgs.append("Errors:\n");
		if (c.iterator().hasNext())
			Log.d("JPT", "Semantic errors:");

		for (Iterator iter2 = c.iterator(); iter2.hasNext();) {
			Problem msg = (Problem) iter2.next();
			Log.d("JPT", msg.toString());
			errorMsgs.append(msg);
		}
		Log.d("JPT:AstTree", unit.dumpTreeNoRewrite());
		Log.d("JPT:PrettyPrint", unit.toString());
	}

	protected void processWarnings(Collection warnings, CompilationUnit unit) {
		if (warnings.iterator().hasNext())
			Log.d("JPT", "Semantic warnings:");
		for (Iterator iter2 = warnings.iterator(); iter2.hasNext();) {
			Problem msg = (Problem) iter2.next();
			Log.d("JPT", msg.toString());
			warningMsgs.append(msg);
		}
	}

	protected void processNoErrors(CompilationUnit unit) {
		Log.d("JPT:AstTree", unit.dumpTreeNoRewrite());
		Log.d("JPT:PrettyPrint", unit.toString());
		normalMsgs.append(unit.dumpTreeNoRewrite());
		normalMsgs.append(unit.toString());
	}

	public void dumpData() {
		System.out.println("\n\nNormal messages:");
		System.out.println(normalMsgs);
		System.out.println("\n\nWarning messages:");
		System.out.println(warningMsgs);
		System.out.println("\n\nError messages:");
		System.out.println(errorMsgs);

	}
}
