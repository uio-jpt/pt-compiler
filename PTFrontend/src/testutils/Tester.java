package testutils;

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import AST.CompilationUnit;
import jargs.gnu.CmdLineParser;

public class Tester {

	private boolean verbose;
	private boolean stopFirst;
	private int totalFail;
	private int totalOK;
	private List<String> notPassed;
	private String[] filenames;

	public static void main(String[] args) {
		Tester tester = parseArgsAndInstantiate(args);
		try {
			tester.runtests();
		} catch (IllegalArgumentException e) {
			System.out.println("Error: " + e.getMessage());
			return;
		} catch (StopFirstException e) {
			// no error
		}
		System.out.println(tester.getSummary());
	}

	private void runtests() {
		for (String filename : filenames) {
			processFilename(filename);
		}
	}

	public void processFilename(String filename) {
		FileIO testFile = new FileIO(filename);

		if (testFile.isDirectory()) {
			performSingleFilesTests(testFile);
			performMultipleFilesTest(testFile);
		} else if (testFile.isFile()) {
			verbose = true;
			runTest(filename);
		} else {
			throw new IllegalArgumentException(filename
					+ " is not a legal path. ");
		}
	}

	private void performSingleFilesTests(FileIO f) {
		Collection<File> files = f.getFilesInFolderAndSubFolders("java");
		for (File file : files) {
			String fileName = file.getAbsolutePath();
			runTest(fileName);
		}
	}

	private void performMultipleFilesTest(FileIO f) {
		Map<String, LinkedList<String>> filesInFolder = f
				.createFolderMap("ptjava");
		for (String folderName : filesInFolder.keySet()) {
			runTest(folderName, filesInFolder.get(folderName));
		}
	}

	private void runTest(String folderName, LinkedList<String> filenames) {
		runTest(new RunTest(folderName, filenames));
	}

	private void runTest(String filename) {
		runTest(new RunTest(filename));
	}

	private void runTest(RunTest test) {
		if (test.didNotRunAsExpected()) {
			printDebugIfVerbose(test);
			totalFail += 1;
			notPassed.add(test.getTestname());
			System.out.println(test.getReport());
			if (stopFirst)
				throw new StopFirstException();
		} else {
			totalOK += 1;
			printNormalDataIfVerbose(test);
		}
	}

	private void printNormalDataIfVerbose(RunTest test) {
		if (verbose) {
			System.out.println(test.getNormalMsgs());
		}
	}

	private void printDebugIfVerbose(RunTest test) {
		if (verbose) {
			System.out.println(test.getWarningMsgs());
			System.out.println(test.getErrorMsgs());
		}
	}

	private int getTotal() {
		return totalFail + totalOK;
	}

	public Tester(boolean isVerbose, boolean isStopFirst, String[] filenames) {
		verbose = isVerbose;
		stopFirst = stopFirst;
		this.filenames = filenames;
		notPassed = new LinkedList<String>();
		totalFail = 0;
		totalOK = 0;
	}

	private String getSummary() {
		StringBuffer sb = new StringBuffer();
		if (totalFail == 0) {
			sb.append("*** All " + getTotal() + " tests passed.\n");
		} else {
			sb.append(String.format(
					"\n*** %d of %d tests passed, %d failed.\n", totalOK,
					getTotal(), totalFail));
			sb.append("The following files did not pass as expected:\n");
			for (String f : notPassed) {
				sb.append("    ant testsingle -Dname=" + f + ".\n");
			}
		}
		return sb.toString();
	}

	private static Tester parseArgsAndInstantiate(String[] args) {
		CmdLineParser parser = new CmdLineParser();
		CmdLineParser.Option verbose = parser.addBooleanOption("verbose");
		CmdLineParser.Option stopFirst = parser.addBooleanOption("stopFirst");
	
		try {
			parser.parse(args);
		} catch (Exception e) {
			System.err.println("Unknown args");
			System.exit(1);
		}
		boolean isVerbose = (Boolean) parser.getOptionValue(verbose,
				Boolean.FALSE);
		boolean isStopFirst = (Boolean) parser.getOptionValue(stopFirst,
				Boolean.FALSE);
	
		String[] filenames = parser.getRemainingArgs();
		Tester tester = new Tester(isVerbose, isStopFirst, filenames);
		return tester;
	}
}
