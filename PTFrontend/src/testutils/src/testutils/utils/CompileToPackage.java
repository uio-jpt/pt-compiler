package testutils.utils;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import testutils.javaparser.PTJavaParser;
import AST.ClassDecl;
import AST.CompilationUnit;
import AST.ImportDecl;
import AST.PTPackage;

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
				StringBuffer source = new StringBuffer();
				source.append(String.format("package %s;\n\n",p.getID()));
				for (ImportDecl id : unit.getImportDeclList())
					source.append(id.toString() + "\n");
				source.append(c.toString() + "\n");
				addSource(p.getID(), c.getID(), source.toString());
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

	public String getCompilableClassData(String packageName, String classname) {
		String ptCode = source.get(createKey(packageName,classname));
		String javaCode = makeJavaCompilable(ptCode);
		return javaCode;
	}

	/**
	 * Replaces every:
	 * tsuper[<classname>]() with tsuper$<classname>$()
	 * tsuper[<templatename.classname>] with tsuper$<templatename$classname>$()
	 * tsuper[<classname>].f() with tsuper$<classname>$f()
	 * tsuper[<templatename.classname>].f() with tsuper$<templatename$classname>$f()
	 */
	private String makeJavaCompilable(String ptCode) {
		String tmp;
		String regSimple = "tsuper\\[(\\w+)\\]\\.?"; 
		String regComplex = "tsuper\\[(\\w+)\\.(\\w+)\\]\\.?";
		String replacementSimple = "tsuper\\$$1\\$";
		String replacementComplex = replacementSimple + "$2\\$";

		Pattern sp = Pattern.compile(regSimple);
		Pattern cp = Pattern.compile(regComplex);
		Matcher ms= sp.matcher(ptCode);
		tmp = ms.replaceAll(replacementSimple);
		Matcher mc= cp.matcher(tmp);
		return mc.replaceAll(replacementComplex);
	}

	public List<String> getSourceWithErrors() {
		return sourceWithErrors;
	}
}