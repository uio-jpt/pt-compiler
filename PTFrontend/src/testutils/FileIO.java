package testutils;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.apache.commons.io.FileUtils;

public class FileIO {

	private File file;

	public FileIO(String filename) {
		file = new File(filename);
	}

	boolean isDirectory() {
		return file.isDirectory();
	}

	public Collection<File> getFilesInFolderAndSubFolders(String extension) {
		String[] filter = {extension};
		boolean recursive = true;
		return FileUtils.listFiles(file, filter, recursive);
	}

	public boolean isFile() { 
		return file.isFile();
	}
	
	public Map<String, LinkedList<String>> createFolderMap(String extensionFilter) {
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

}
