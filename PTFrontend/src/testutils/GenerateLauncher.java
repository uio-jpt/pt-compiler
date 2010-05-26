package testutils;

import java.io.IOException;

import javax.xml.transform.Source;

public class GenerateLauncher {

	public static void main(String[] args) {
		String source = null;
		if (args.length != 2)
			usage();
		FileIO sourcefile = new FileIO(args[0]);
		if (!sourcefile.isFile())
			usage();
		String cwd = System.getProperty("user.dir");
		try {
			source = sourcefile.readFile();
		} catch (IOException e) {
			error("unable to read file: " + sourcefile.getAbsolutePath());
		}
		String newSource = source.replaceFirst("BASEDIR", cwd);
		FileIO launcher = new FileIO(args[1]);
		launcher.write(newSource);
		launcher.setExecutable(true);
	}

	private static void error(String message) {
		System.err.println(message);
		usage();

	}

	private static void usage() {
		System.err
				.println("Input must be location of generic python launcher file");
		System.exit(1);
	}
}
