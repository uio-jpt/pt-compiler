package testutils.tester;

import java.util.LinkedList;
import java.util.Map;

public class FilesInFolderIsUnitSuite extends FolderTestSuite implements TestSuiteView {


	public FilesInFolderIsUnitSuite(String foldername) {
		super(foldername);
	}

	public void runSuite(ReportManager testReports) {
        String [] extensions = { "java", "jpt", "javapt", "ptjava" };
		Map<String, LinkedList<String>> filesInFolder = folder.createFolderMap( extensions );
		for (String folderName : filesInFolder.keySet()) {
			TestCase test = new TestRunner(folderName, filesInFolder.get(folderName));
			test.run(testReports);
		}
	}
}
