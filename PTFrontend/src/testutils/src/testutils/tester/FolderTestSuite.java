package testutils.tester;

import testutils.exceptions.FatalErrorException;
import testutils.utils.FileIO;

public abstract class FolderTestSuite {
	
	protected FileIO folder;

	public FolderTestSuite(String foldername) {
		folder = new FileIO(foldername);
		if (!folder.isDirectory())
			throw new FatalErrorException("folder: " + foldername + "not found");
	}
}
