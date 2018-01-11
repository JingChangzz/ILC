package edu.psu.cse.siis.ic3;

import edu.psu.cse.siis.ic3.db.SQLConnection;
import edu.psu.cse.siis.ic3.manifest.SHA256Calculator;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

public class Ic3Main {
	public static String manifest;
	public static void main(String[] args, String manifestPath) throws SQLException {
		edu.psu.cse.siis.coal.Main.reset();
		SQLConnection.reset();
		manifest = manifestPath;
		Ic3CommandLineParser parser = new Ic3CommandLineParser();
		Ic3CommandLineArguments commandLineArguments = parser.parseCommandLine(args, Ic3CommandLineArguments.class);
		if (commandLineArguments == null) {
			return;
		}
		commandLineArguments.processCommandLineArguments();

		SQLConnection.init(commandLineArguments.getDbName(), "./db.properties", null, 3306);
		try {
			String shasum = SHA256Calculator.getSHA256(new File(commandLineArguments.getInput()));
			if (SQLConnection.checkIfAppAnalyzed(shasum)) {
				return;
			}

			Ic3Analysis analysis = new Ic3Analysis(commandLineArguments);
			analysis.performAnalysis(commandLineArguments);
		} catch (SQLException | NoSuchAlgorithmException | IOException e) {
			System.out.println("Error computing SHA of apk file!");
		}

	}

}
