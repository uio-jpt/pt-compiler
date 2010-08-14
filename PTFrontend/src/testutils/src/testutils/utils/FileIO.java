package testutils.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.apache.commons.io.FileUtils;

public class FileIO extends File {

	public FileIO(String filename) {
		super(filename);
	}

	public String[] getFilePaths(String extension) {
		ArrayList<String> filenames = new ArrayList<String>();
		Collection<File> files = getFilesInFolderAndSubFolders(extension);
		for (File file : files) 
			filenames.add(file.getPath());
		return filenames.toArray(new String[filenames.size()]);
	}

	public Collection<File> getFilesInFolderAndSubFolders(String extension) {
		String[] filter = { extension };
		boolean recursive = true;
		return FileUtils.listFiles(this, filter, recursive);
	}

	public Map<String, LinkedList<String>> createFolderMap(
			String extensionFilter) {
		Collection<File> files = getFilesInFolderAndSubFolders(extensionFilter);
		Map<String, LinkedList<String>> folderMap = new HashMap<String, LinkedList<String>>();
		for (File file : files) {
			String absPath = file.getAbsolutePath();
			String folder = file.getParent();
			if (!folderMap.containsKey(folder)) {
				folderMap.put(folder, new LinkedList<String>());
			}
			folderMap.get(folder).add(absPath);
		}
		return folderMap;
	}

	public FileIO createExtendedPath(String packageName) {
		 String path = getPath() + File.separator + packageName;
		return new FileIO(path);
	}

	public void write(String data) {
		try {
			FileUtils.writeStringToFile(this, data);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1); // TODO something clever
		}
	}

	public String readFile() throws IOException {
		return FileUtils.readFileToString(this);
	}

	public FileIO skipExtension() {
		String path = getPath();
		int splitIndex = path.lastIndexOf(".");
		if (splitIndex > 0) {
			path = path.substring(0,splitIndex);
		}
		return new FileIO(path);
	}
}
