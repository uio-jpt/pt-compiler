package testutils.tester;

import testutils.exceptions.FatalErrorException;
import testutils.utils.FileIO;

public class SingleFileNamesSuite implements TestSuiteView {

	private String[] singleFilenames;

	public SingleFileNamesSuite(String[] singleFiles) {
		this.singleFilenames = singleFiles;
	}

	@Override
	public void runSuite(ReportManager testReports) {
		for (String filename : singleFilenames) {
			FileIO f = new FileIO(filename);
			if (!f.isFile()) {
				throw new FatalErrorException(filename + " is not a directory.");
			}
			TestCase test = new TestRunner(filename);
			test.run(testReports);
		}
	}
}
