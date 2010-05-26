package testutils;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.filefilter.SuffixFileFilter;

import testutils.javaparser.PTJavaParser;
import AST.ClassDecl;
import AST.CompilationUnit;
import AST.JavaParser;
import AST.PTCompilationUnit;
import AST.PTDecl;
import AST.PTPackage;
import AST.SimpleClass;

public class CompileToPackage extends PTFrontend {

	private Map<String,List<String>> packageNameToClassNames;
	private Map<String, String> source;
	private List<String> sourceWithErrors;

	/** Use default JavaParser */
	public CompileToPackage(String[] filenames) {
		super(filenames, new PTJavaParser());
		packageNameToClassNames = new HashMap<String, List<String>>();
		source = new HashMap<String, String>();
		sourceWithErrors = new LinkedList<String>();
	}

	public Iterable<String> getPackageNames() {
		return packageNameToClassNames.keySet();
	}
	
	@Override
	protected void processErrors(Collection errors, CompilationUnit unit) {
		super.processErrors(errors, unit);
		sourceWithErrors.add(unit.toString());
	}

	@Override
	protected void processNoErrors(CompilationUnit unit) {
		super.processNoErrors(unit);
		Collection<PTPackage> localPackages = unit.getPTPackages();
		for (PTPackage p : localPackages) {
			List<String> classNames = new LinkedList<String>();
			for (ClassDecl c : p.getClassList()) {
				classNames.add(c.getID());
				addSource(p.getID(), c.getID(), c.toString());
			}
			packageNameToClassNames.put(p.getID(),classNames);
		}
	}

	private void addSource(String packagename, String classname, String classSource) {
		source.put(createKey(packagename,classname),classSource);
	}
	
	private String createKey(String packagename, String classname) {
		return packagename + "$" + classname;
	}

	public Iterable<String> getClassnames(String packageName) {
		return packageNameToClassNames.get(packageName);
	}

	public String getClassData(String packageName, String classname) {
		return source.get(createKey(packageName,classname));
	}

	public List<String> getSourceWithErrors() {
		return sourceWithErrors;
	}
}
