package testutils.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.CodeSource;
import java.util.Scanner;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.apache.commons.io.IOUtils;

import testutils.PTToJavaPackage;
import testutils.exceptions.FatalErrorException;

public class GenericBuildXML {

	private static final String GENERIC_BUILD_XML_LOCATION = "txt/generic-build.xml";

	private static String readGenericBuildFile(File jarPath) {
		try {
			StringWriter writer = new StringWriter();
			JarFile jar = new JarFile(jarPath);
			ZipEntry entry = jar.getEntry(GENERIC_BUILD_XML_LOCATION);
			InputStream stream = jar.getInputStream(entry);
			IOUtils.copy(stream, writer);
			return writer.toString();
		} catch (Exception e) {
			throw new FatalErrorException(String.format(
					"Couldn't read file %s from jarfile",
					GENERIC_BUILD_XML_LOCATION));
		}
	}

	private static File getJarPath() {
		CodeSource codeSource = PTToJavaPackage.class.getProtectionDomain()
				.getCodeSource();

		URL url = codeSource.getLocation();
		URI uri;
		try {
			uri = new URI(url.getPath());
			return new File(uri.getPath());
		} catch (URISyntaxException e) {
			throw new FatalErrorException("unable to locate JAR file");
		}
	}

	public static String generateBuildFile(String outputFolderName) {
		String genericBuildFile;
		try {
			File jarPath = getJarPath();
			genericBuildFile = readGenericBuildFile(jarPath);
		} catch (FatalErrorException e) {
			// happens when run directly from class files instead of jar file
			genericBuildFile = readBuildfileFromFileSystem();
		}
		String buildFileText = genericBuildFile.replaceFirst("PROJECTNAME",
				outputFolderName);
		return buildFileText;
	}

	private static String readBuildfileFromFileSystem() {
		Scanner scanner;
		File file = new File("resources/txt/generic-build.xml");
		try {
			scanner = new Scanner(file);
			StringBuffer sb = new StringBuffer();
			while (scanner.hasNextLine()) {
				sb.append(scanner.nextLine() + "\n");
			}
			return sb.toString();
		} catch (FileNotFoundException e) {
			throw new FatalErrorException(
					"Unable to locate jar file and failed to read generic-build.xml from the filesystem");
		}

	}
}
