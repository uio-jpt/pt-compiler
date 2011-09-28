package testutils.tester;

import java.io.File;
import java.util.Collection;

public class FileInFolderIsSingleUnitSuite extends FolderTestSuite implements
		TestSuiteView {

	public FileInFolderIsSingleUnitSuite(String foldername) {
		super(foldername);
	}

	public void runSuite(ReportManager testReports) {
        String [] extensions = { "java", "jpt", "javapt", "ptjava" };
		Collection<File> files = folder.getFilesInFolderAndSubFolders( extensions );
		for (File file : files) {
			String fileName = file.getPath();
			TestCase test = new TestRunner(fileName);
			test.run(testReports);
		}		
	}
}
