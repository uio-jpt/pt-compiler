package testutils;

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import AST.CompilationUnit;
import jargs.gnu.CmdLineParser;

/**
 * TODO Koden her har sklidd litt ut. bør deles opp i et par klasser til...
 * 
 * @author eivindgl
 * 
 */
public class Tester {

	private boolean verbose;
	private boolean stopFirst;
	private int totalFail;
	private int totalOK;
	private List<String> notPassed;
	private String singleFileFolder;
	private String multipleFileFolder;
	private boolean isSingle;
	private String singleFilename;

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
		if (isSingle) {
			File f = new File(singleFilename);
			if (f.isDirectory())
				multipleFileFolder = singleFilename;
			else
				singleFileFolder = singleFilename;
		}
		performSingleFilesTests();
		performMultipleFilesTest();
	}

	private void performSingleFilesTests() {
		if (singleFileFolder == null) return;
		FileIO f = new FileIO(singleFileFolder);
		if (!f.isDirectory()) {
			if (f.isFile()) {
				runTest(singleFileFolder);
				return;
			}
			throw new IllegalArgumentException(singleFileFolder
					+ " is not a directory.");
		}
		Collection<File> files = f.getFilesInFolderAndSubFolders("java");
		for (File file : files) {
			String fileName = file.getPath();
			runTest(fileName);
		}
	}

	private void performMultipleFilesTest() {
		if (multipleFileFolder == null) return;
		System.out.println("multiple: " + multipleFileFolder);
		FileIO f = new FileIO(multipleFileFolder);
		if (!f.isDirectory())
			throw new IllegalArgumentException(multipleFileFolder
					+ " is not a directory.");
		Map<String, LinkedList<String>> filesInFolder = f
				.createFolderMap("java");
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
			String normalMsgs = test.getNormalMsgs();
			if (!normalMsgs.isEmpty())
				System.out.println("verbose normal:\n" + normalMsgs);

			String warningMsgs = test.getWarningMsgs();
			if (!warningMsgs.isEmpty())
				System.out.println("verbose warning:\n" + warningMsgs);

			String errorMsgs = test.getErrorMsgs();
			if (!errorMsgs.isEmpty())
				System.out.println("verbose error:\n" + errorMsgs);
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

	public Tester() {
		notPassed = new LinkedList<String>();
		totalFail = 0;
		totalOK = 0;
	}

	public Tester(boolean isVerbose, boolean isStopFirst,
			String singleFileFolder, String multipleFileFolder) {
		this();
		verbose = isVerbose;
		stopFirst = isStopFirst;
		this.singleFileFolder = singleFileFolder;
		this.multipleFileFolder = multipleFileFolder;
		isSingle = false;
	}

	public Tester(String filename) {
		this();
		isSingle = true;
		singleFilename = filename;
		verbose = true;
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
				sb.append("    ant testsingle -Dname=" + f + "\n");
			}
		}
		return sb.toString();
	}

	private static Tester parseArgsAndInstantiate(String[] args) {
		CmdLineParser parser = new CmdLineParser();
		CmdLineParser.Option verbose = parser.addBooleanOption("verbose");
		CmdLineParser.Option stopFirst = parser.addBooleanOption("stopFirst");
		CmdLineParser.Option testSingleDirOption = parser
				.addStringOption("testSingleFiles");
		CmdLineParser.Option testMultipleDirOption = parser
				.addStringOption("testFolderAsUnit");

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

		Tester tester;
		String singleFileFolder = (String) parser
				.getOptionValue(testSingleDirOption);
		String multipleFilesFolder = (String) parser
				.getOptionValue(testMultipleDirOption);

		if (singleFileFolder != null || multipleFilesFolder != null)
			tester = new Tester(isVerbose, isStopFirst, singleFileFolder,
					multipleFilesFolder);
		else {
			String[] filenames = parser.getRemainingArgs();
			if (filenames.length != 1)
				throw new IllegalArgumentException(
						"Must test a single file or (singleFileFolderOption and/or multipleFileFolderOption). None given. TODO nicer output..");
			tester = new Tester(filenames[0]);
		}
		return tester;
	}
}
