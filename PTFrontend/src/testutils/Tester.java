package testutils;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;

import AST.CompilationUnit;
import AST.JavaParser;


/** Has ugly proof of concept code to test all files ending with .ptjava in the same folder together.
 *   
 * 
 * @author eivindgl
 *
 */
public class Tester extends PTFrontend {

	public Tester(String[] args, JavaParser parser) {
		super(args, parser);
	}

	protected void processErrors(java.util.Collection errors,
			CompilationUnit unit) {
		// System.out.println(unit.dumpTreeNoRewrite());
		// System.out.println(unit.toString());
		if (Tester.shouldBeOk || Tester.isSingle || Tester.verbose)
			super.processErrors(errors, unit);
	}

	protected void processWarnings(java.util.Collection warnings,
			CompilationUnit unit) {
		if (Tester.shouldBeOk || Tester.isSingle)
			super.processWarnings(warnings, unit);
	}

	protected void processNoErrors(CompilationUnit unit) {
		if (Tester.verbose)
			System.out.println(unit.dumpTreeNoRewrite());
		if (Tester.verbose)
			System.out.println(unit.toString());
	}

	public static boolean verbose = false;
	public static boolean stopFirst = false;
	public static boolean isSingle = false;
	public static int total = 0;
	public static int totalFail = 0;
	public static int totalOK = 0;
	public static boolean shouldBeOk;
	public static LinkedList<String> notPassed = new LinkedList<String>();

	public static void doDir(String dir) {
		File f = new File(dir);

		if (f.isDirectory()) {
			String[] p = { "java" };
			Collection<File> files = FileUtils.listFiles(f, p, true);
			for (File file : files) {
				String fileName = file.getAbsolutePath();
				doFile(fileName);
			}
			String[] pt = { "ptjava" };
			Collection<File> ptfiles = FileUtils.listFiles(f, pt, true);
			Map<String,LinkedList<String>> filesInFolder = createFolderMap(ptfiles);
			for (String folder : filesInFolder.keySet()) {				
                            doFiles(folder,filesInFolder.get(folder));
			}
		} else if (f.isFile()) {
			isSingle = true;
			doFile(dir);
		}
	}

    public static void doFile(String fileName) {
		if (stopFirst && totalFail > 0)
			return;
		total += 1;
		shouldBeOk = !fileName.contains("_fail");
		boolean ok = Tester.compile(fileName);
		if (shouldBeOk != ok) {
			totalFail += 1;
			String desiredResult = shouldBeOk == true ? "passed" : "failed";
			String result = shouldBeOk != true ? "passed" : "failed";
			System.out.println("Test for filename '" + fileName + "' (" + total
					+ ") should have " + desiredResult + ", but " + result);
			notPassed.add(fileName);
		} else {
			totalOK += 1;
		}
	}

	public static void doFiles(String folder, Collection<String> files) {
		total += 1;
		shouldBeOk = !folder.endsWith("_fail");
		boolean ok = Tester.compile(files.toArray(new String[files.size()]));
		if (shouldBeOk != ok) {
			String desiredResult = shouldBeOk == true ? "passed" : "failed";
			String result = shouldBeOk != true ? "passed" : "failed";
			System.out.println("Test for files in folder '" + folder + "' (" + total
					+ ") should have " + desiredResult + ", but " + result);
			notPassed.add(folder);
			totalFail += 1;			
			System.out.println("Test for filename folder[TODO]'" + "' (" + total
					+ ") should have " + desiredResult + ", but " + result);
			notPassed.add("folder" + files.hashCode());
		} else {
			totalOK += 1;
		}
	}

	private static Map<String, LinkedList<String>> createFolderMap(
			Collection<File> ptfiles) {
		Map<String, LinkedList<String>> folderMap = new HashMap<String, LinkedList<String>>();
		for (File file : ptfiles) {
			String absPath = file.getAbsolutePath();
			String folder = file.getParent();
			if (!folderMap.containsKey(folder)) {
				folderMap.put(folder,new LinkedList<String>());
			}
			folderMap.get(folder).add(absPath);
		}
		return folderMap;
	}

	public static void main(String[] args) {
		
		for (String f : args) {
			if (f.equals("--stopfirst")) {
				stopFirst = true;
				continue;
			}
			if (f.contains("--verbose")) {
				verbose = true;
				continue;
			}
			doDir(f);
		}
		if (totalOK == total) {
			System.out.println("*** All " + total + " tests passed.");
		} else {
			System.out.println(String.format(
					"\n*** %d of %d tests passed, %d failed.", totalOK, total,
					totalFail));
			System.out.println("The following files did not pass as expected:");
			for (String f : notPassed) {
				System.out.println("    ant testsingle -Dname=" + f);
			}
		}
	}
}
