package testutils.tester;

import java.io.File;

public class SimpleReport {
	String name;
	String[] filenames;
	boolean actual;

	public SimpleReport(String name, String[] filenames, boolean actual) {
		this.name = name;
		this.filenames = filenames;
		this.actual = actual;
	}
	
	public String getPath() {
		return new File(name).getPath();
	}

	public String[] getInputfilenames() {
		return filenames;
	}
}