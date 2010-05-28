package testutils;

import jargs.gnu.CmdLineParser;

import java.io.File;

import testutils.exceptions.CompileErrorException;
import testutils.exceptions.FatalErrorException;
import testutils.utils.FileIO;

public class PTToJavaPackage {

	private String sourceFolderName;
	private String outputFolderName;
	private boolean verbose;
	private String[] inputfilenames; 
	private GenerateJava compiler;

	public static void main(String[] args) {
		try {
			PTToJavaPackage controller = parseArgsAndInstantiate(args);
			controller.readSourceFolder();
			controller.compileAndWrite();
			System.out
					.println("Compilation completed. Java package(s) written to "
							+ controller.outputFolderName);
		} catch (FatalErrorException e) {
			System.out.println("Fatal error:");
			System.out.println(e.getMessage());
		} catch (CompileErrorException e) {
			System.out.println("Compilation had errors.");
		}
	}

	private void compileAndWrite() {
		compiler = new GenerateJava(inputfilenames, outputFolderName);
		compiler.compile();
		compiler.write();
	}

	private void error(String message) {
		throw new FatalErrorException(message);

	}

	private void readSourceFolder() {
		FileIO sourceFolder;
		try {
			sourceFolder = new FileIO(sourceFolderName);
			verbose("SourceFolderName: " + sourceFolderName);
		} catch (NullPointerException e) {
			sourceFolder = null;
			error("Missing sourceFolder");
		}
		if (!sourceFolder.isDirectory())
			error(String.format("Source folder directory '%s' not found.",
					sourceFolderName));
		inputfilenames = sourceFolder.getFilePaths("java");
		verbose("Printing all [" + inputfilenames.length + "] inputfilenames.");
		for (String filename : inputfilenames)
			verbose("\tinputfilename: " + filename);
	}

	private void verbose(String string) {
		if (verbose)
			System.out.println("Verbose: " + string);
	}

	private static PTToJavaPackage parseArgsAndInstantiate(String[] args) {
		CmdLineParser parser = new CmdLineParser();
		CmdLineParser.Option verboseOption = parser.addBooleanOption('v',
				"verbose");
		CmdLineParser.Option outputFolderOption = parser.addStringOption('o',
				"outputFolder");
		CmdLineParser.Option genericBuildXMLPathOpt = parser.addStringOption(
				'p', "buildXMLPath");

		try {
			parser.parse(args);
		} catch (Exception e) {
			System.err.println("Unknown args: " + e.getMessage());
			System.exit(1);
		}
		boolean verbose = (Boolean) parser.getOptionValue(verboseOption,
				Boolean.FALSE);

		String[] remainingArgs = parser.getRemainingArgs();
		if (remainingArgs.length == 0)
			throw new FatalErrorException("couldn't find an input file.");
		String sourceFolder = new File(remainingArgs[0]).getPath();
		String outputFolder = (String) parser.getOptionValue(
				outputFolderOption, sourceFolder + "_output");
		PTToJavaPackage controller = new PTToJavaPackage(sourceFolder,
				outputFolder, verbose);
		controller.verbose("Verbose flag turned on.");
		return controller;
	}

	public PTToJavaPackage(String sourceFolder, String outputFolder,
			boolean verbose) {
		super();
		this.sourceFolderName = sourceFolder;
		this.outputFolderName = outputFolder;
		this.verbose = verbose;
	}
}
