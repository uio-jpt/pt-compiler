package testutils;

import java.io.File;

import jargs.gnu.CmdLineParser;

public class PTToJavaPackage {
	private String sourceFolderName;
	private String outputFolderName;
	private boolean verbose;
	private FileIO sourceFolder;
	private String[] inputfileNames;
	private FileIO outputFolder;
	private CompileToPackage compilerInterface;

	public static void main(String[] args) {
		PTToJavaPackage controller = parseArgsAndInstantiate(args);
		controller.checkArgs();
		controller.buildAST();
		controller.writePackages();
	}
	
	private void checkArgs() {
		printArgsIfVerbose();
		if (sourceFolderName == null || outputFolderName == null)
			throw new IllegalArgumentException("Missing sourceFolder or outputFolder");
		sourceFolder = new FileIO(sourceFolderName);
		if (!sourceFolder.isDirectory())
			throw new IllegalArgumentException(String.format("Source folder directory '%s' not found.", sourceFolderName));
		inputfileNames = sourceFolder.getFilesAsAbsolutePaths("java");
		outputFolder = new FileIO(outputFolderName);
		if (outputFolder.exists()) {
			if (!outputFolder.isDirectory())
				throw new IllegalArgumentException(String.format("Destination path '%s' exists and is not a directory.", outputFolderName));
		} else {
			boolean outputFolderCreated = outputFolder.mkdirs();
			if (!outputFolderCreated)
				throw new IllegalArgumentException(String.format("Destination path '%s' did not exist and couldn't be created.", outputFolderName));
		}
		if (verbose)
			for (String filename : inputfileNames) 
				System.out.println("inputfilename: " + filename);
	}

	private void printArgsIfVerbose() {
		if (verbose) {
			System.out.println("SourceFolderName: " + sourceFolderName);
			System.out.println("OutputFolderName: " + outputFolderName);
		}
		
	}

	private void buildAST() {
		compilerInterface = new CompileToPackage(inputfileNames);
		boolean result = compilerInterface.process();
		if (verbose)
			System.out.println("Compilation done " + (result ? "without" : "with") + " errors.");
	}

	private void writePackages() {
		for (String packageName : compilerInterface.getPackageNames()) {
			FileIO packageFolder = outputFolder.createExtendedPath(packageName);
			packageFolder.mkdir();
			for (String classname : compilerInterface.getClassnames(packageName)) {
				FileIO classFile = packageFolder.createExtendedPath(classname + ".java");
                                String source = String.format("package %s;\n\n",packageName);
                                source += compilerInterface.getClassData(packageName,classname);
                                source += "\n";
				classFile.write(source);
			}
		}
		
	}

	private static PTToJavaPackage parseArgsAndInstantiate(String[] args) {
		CmdLineParser parser = new CmdLineParser();
		CmdLineParser.Option verboseOption = parser.addBooleanOption("verbose");
		CmdLineParser.Option sourceFolderOption = parser.addStringOption('s', "sourceFolder");
		CmdLineParser.Option outputFolderOption = parser.addStringOption('o', "outputFolder");
	
		try {
			parser.parse(args);
		} catch (Exception e) {
			System.err.println("Unknown args: " + e.getMessage());
			System.exit(1);
		}
		boolean verbose = (Boolean) parser.getOptionValue(verboseOption,
				Boolean.FALSE);
		String sourceFolder = (String) parser.getOptionValue(sourceFolderOption);
		String outputFolder = (String) parser.getOptionValue(outputFolderOption);
	
		PTToJavaPackage controller = new PTToJavaPackage(sourceFolder, outputFolder, verbose);
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
