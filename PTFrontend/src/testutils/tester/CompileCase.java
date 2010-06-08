package testutils.tester;

import java.io.File;
import java.io.IOException;

import testutils.exceptions.CompileErrorException;

public class CompileCase implements TestCase {

	private String[] inputfoldernames;
	private String outputFolderPath;
	private static String[] ANT_BUILD = {"ant", "build"};

	public CompileCase(String[] inputfoldernames, String outputFolderPath) {
		this.inputfoldernames = inputfoldernames;
		this.outputFolderPath = outputFolderPath;
	}

	public void run(ReportManager testReports) {
		GenerateJava ptcompiler = new GenerateJava(inputfoldernames, outputFolderPath);
		ptcompiler.compile();
		ptcompiler.write();
		Runtime r = Runtime.getRuntime();
		File dir = new File(outputFolderPath);
		Process p;
		try {
			p = r.exec(ANT_BUILD, null, dir);
		} catch (IOException e) {
			System.out.println(e.getMessage());
			System.out.println(e.getStackTrace());
			throw new CompileErrorException("Javac compiler subprocess borked. See testutils.tester.CompileCase");
		}
		try {
			p.waitFor();
		} catch (InterruptedException e) {
			throw new CompileErrorException("Should this occur? See testutils.tester.CompileCase");
		}
		boolean isWithoutError = p.exitValue() == 0;
		SimpleReport report = new SimpleReport(outputFolderPath, inputfoldernames, isWithoutError);
		testReports.addReport(report);

	}

}
