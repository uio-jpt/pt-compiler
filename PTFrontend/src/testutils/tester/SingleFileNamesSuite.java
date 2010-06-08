package testutils.tester;

import testutils.exceptions.FatalErrorException;
import testutils.utils.FileIO;

public class SingleFileNamesSuite implements TestSuiteView {

	private String filename;

	public SingleFileNamesSuite(String name) {
		filename = name;
	}

	@Override
	public void runSuite(ReportManager testReports) {
		FileIO f = new FileIO(filename);
		if (!f.isFile()) {
			throw new FatalErrorException(filename + " is not a regular file.");
		}
		TestCase test = new TestRunner(filename);
		test.run(testReports);
	}
}
