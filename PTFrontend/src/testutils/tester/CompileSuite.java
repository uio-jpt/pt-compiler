package testutils.tester;

import java.util.List;

import testutils.utils.FileIO;

public class CompileSuite implements TestSuiteView {

	private FileIO baseFolder;
	private List<SemanticReport> compilableTests;

	public CompileSuite(String compileTestsOutputFolder, List<SemanticReport> compilableTests) {
		baseFolder = new FileIO(compileTestsOutputFolder);
		this.compilableTests = compilableTests;
	}

	@Override
	public void runSuite(ReportManager testReports) {
		for (SemanticReport report: compilableTests) {
			String outputFolderPath = getOutputfolderName(report.getPath());
			String [] inputfoldernames = report.getInputfilenames();
			TestCase test = new CompileCase(inputfoldernames, outputFolderPath);
			test.run(testReports);
		}

	}

	private String getOutputfolderName(String path) {
		FileIO newPath = baseFolder.createExtendedPath(path).skipExtension();
		return newPath.getPath();
	}

}
