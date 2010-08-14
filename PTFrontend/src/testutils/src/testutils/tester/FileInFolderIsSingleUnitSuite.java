package testutils.tester;

import java.io.File;
import java.util.Collection;

public class FileInFolderIsSingleUnitSuite extends FolderTestSuite implements
		TestSuiteView {

	public FileInFolderIsSingleUnitSuite(String foldername) {
		super(foldername);
	}

	public void runSuite(ReportManager testReports) {
		Collection<File> files = folder.getFilesInFolderAndSubFolders("java");
		for (File file : files) {
			String fileName = file.getPath();
			TestCase test = new TestRunner(fileName);
			test.run(testReports);
		}		
	}
}
