package testutils.tester;

import testutils.exceptions.CompileErrorException;
import testutils.exceptions.FatalErrorException;
import testutils.utils.CompileToPackage;
import testutils.utils.FileIO;
import testutils.utils.GenericBuildXML;

public class GenerateJava {

	private FileIO outputBaseFolder;
	private CompileToPackage compilerInterface;
	private FileIO outputSrcFolder;

	public GenerateJava(String[] inputfileNames, String outputFolderName) {
		outputBaseFolder = new FileIO(outputFolderName);
		compilerInterface = new CompileToPackage(inputfileNames);
	}

	public void compile() {
		compilerInterface.process();
		
		if (hasErrors()) {
			String errorMsg = getErrorReport();
			throw new CompileErrorException(errorMsg);
		}
	}

	public void write() {
		prepareOutputFolder();
		writeBuildXML();
		writePackages();
	}

	private boolean hasErrors() {
		return !compilerInterface.getErrorMsgs().equals("");
	}

	private void writeBuildXML() {
		FileIO buildFile = outputBaseFolder.createExtendedPath("build.xml");
		String src = GenericBuildXML.generateBuildFile(outputBaseFolder.getName());
		buildFile.write(src);
	}

	private void writePackages() {
		for (String packageName : compilerInterface.getPackageNames()) {
			FileIO packageFolder = outputSrcFolder
					.createExtendedPath(packageName);
			packageFolder.mkdir();
			writePackage(packageFolder);
		}
	}

	private void writePackage(FileIO packageFolder) {
		String packageName = packageFolder.getName();
		for (String classname : compilerInterface.getClassnames(packageName)) {
			FileIO classFile = packageFolder.createExtendedPath(classname
					+ ".java");
			String source = compilerInterface.getCompilableClassData(packageName,
					classname);
			source += "\n";
			classFile.write(source);
		}
	}

	private String getErrorReport() {
		StringBuilder sb = new StringBuilder();
		for (String errorSource : compilerInterface.getSourceWithErrors()) {
			sb.append(errorSource + "\n");
		}
		sb.append("Error: " + compilerInterface.getErrorMsgs());
		return sb.toString();
	}

	public void prepareOutputFolder() {
		String outputFolderName = outputBaseFolder.getPath();
		outputSrcFolder = outputBaseFolder.createExtendedPath("src");
		if (outputSrcFolder.exists()) {
			if (!outputSrcFolder.isDirectory())
				throw new IllegalArgumentException(String.format(
						"Destination path '%s' exists and is not a directory.",
						outputFolderName));
		} else {
			boolean outputFolderCreated = outputSrcFolder.mkdirs();
			if (!outputFolderCreated)
				new FatalErrorException(
						String
								.format(
										"Destination path '%s' did not exist and couldn't be created.",
										outputFolderName));
		}
		if (!outputSrcFolder.exists())
			outputSrcFolder.mkdir();

	}
}
