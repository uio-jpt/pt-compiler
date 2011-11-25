package testutils.tester;

import java.util.List;

import testutils.javaparser.PTJavaParser;
import testutils.utils.PTFrontend;
import AST.CompilationUnit;
import AST.JavaParser;

public class TestRunner extends PTFrontend implements TestCase {

	private boolean shouldBeOk, actual;
	private String[] filenames;
	private String name;

	public TestRunner(String filename) {
		this(asArray(filename));
	}
	
	/** Use default JavaParser */
	public TestRunner(String foldername, List<String> filenames) {
		this(foldername, asArray(filenames), new PTJavaParser());
	}

	private static String[] asArray(List<String> seq) {
		return seq.toArray(new String[seq.size()]);
	}

	/** Use default JavaParser */
	public TestRunner(String[] filenames) {
		this(filenames[0], filenames, new PTJavaParser());
	}

	public TestRunner(String testname, String[] filenames, JavaParser parser) {
		super(filenames, parser);
		this.filenames = filenames;
		this.name = testname; 
		this.shouldBeOk = !testname.contains("_fail");
	}

	private static String[] asArray(String filename) {
		String[] x = { filename };
		return x;
	}

	public void run(ReportManager testReports) {
            System.out.println( "Running test: " + name ); /* this is terribly useful when one test _crashes_ or gives unexpected
                                                              output on stdout/err without giving any errors. TODO transform
                                                              this into --verbose mode */
			SemanticReport report = null;
            try {
                actual = process();
            }
            catch( Exception e ) {
                report = new SemanticTotalFailureReport(name, filenames, e, containsPTPackage() );
            }
            if( report == null ) {
                report = generateReport();
            }
			testReports.addReport(report);
	}

	private SemanticReport generateReport() {
		boolean containsPTPackage = containsPTPackage(); 
		return new SemanticReport(name,filenames,shouldBeOk,actual,getNormalMsgs(),getWarningMsgs(),getErrorMsgs(),containsPTPackage);
	}

	private boolean containsPTPackage() {
		for (CompilationUnit unit : program.getCompilationUnits()) 
			if (!unit.getPTPackages().isEmpty())
				return true;
		return false;
	}
}
