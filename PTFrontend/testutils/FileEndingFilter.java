package testutils;

import java.io.File;
import java.io.FileFilter;

public class FileEndingFilter implements FileFilter{
	private String fileEnding = null;
	public FileEndingFilter(String fileEnding) {
		this.fileEnding = fileEnding;
	}
	public boolean accept(File pathname) {
		if(pathname.getName().endsWith("." + this.fileEnding))
			return true;
		else
			return false;
	}
}
