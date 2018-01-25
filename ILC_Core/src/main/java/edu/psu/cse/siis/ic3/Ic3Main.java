package edu.psu.cse.siis.ic3;

import edu.psu.cse.siis.ic3.manifest.SHA256Calculator;
import ilc.db.ILCSQLConnection;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Set;

public class Ic3Main {
	public static String manifest;
	public static Set<String> entryPointClasses;
	public static void main(String[] args, String manifestPath, Set<String> entryPoints) throws SQLException {
		edu.psu.cse.siis.coal.Main.reset();
		ILCSQLConnection.reset();
		manifest = manifestPath;
		entryPointClasses = entryPoints;
		Ic3CommandLineParser parser = new Ic3CommandLineParser();
		Ic3CommandLineArguments commandLineArguments = parser.parseCommandLine(args, Ic3CommandLineArguments.class);
		if (commandLineArguments == null) {
			return;
		}
		commandLineArguments.processCommandLineArguments();

		ILCSQLConnection.init(commandLineArguments.getDbName(), "./db.properties", null, 3306);
		try {
			String shasum = SHA256Calculator.getSHA256(new File(commandLineArguments.getInput()));
//			if (ILCSQLConnection.checkIfLibAnalyzed(shasum)) {
//				return;
//			}

			Ic3Analysis analysis = new Ic3Analysis(commandLineArguments);
			analysis.performAnalysis(commandLineArguments);
		} catch (NoSuchAlgorithmException | IOException e) {
			System.out.println("Error computing SHA of apk file!");
		}

	}

}
