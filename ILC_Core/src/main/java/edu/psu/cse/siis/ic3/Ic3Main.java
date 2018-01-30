package edu.psu.cse.siis.ic3;

import ilc.data.ParseJar;
import ilc.db.ILCSQLConnection;

import java.sql.SQLException;
import java.util.Set;

public class Ic3Main {
	public static String manifest;
	public static Set<String> entryPointClasses;
	public static boolean isPlainEn;
	public static void main(String[] args, String manifestPath, boolean isPlain) throws SQLException {
		edu.psu.cse.siis.coal.Main.reset();
		ILCSQLConnection.reset();
		manifest = manifestPath;
		if (isPlain){
			entryPointClasses = ParseJar.plainEntryPoints;
		}else {
			entryPointClasses = ParseJar.entryPointsForAndroid;
		}
		isPlainEn = isPlain;
		Ic3CommandLineParser parser = new Ic3CommandLineParser();
		Ic3CommandLineArguments commandLineArguments = parser.parseCommandLine(args, Ic3CommandLineArguments.class);
		if (commandLineArguments == null) {
			return;
		}
		commandLineArguments.processCommandLineArguments();

		ILCSQLConnection.init(commandLineArguments.getDbName(), "./db.properties", null, 3306);
//			String shasum = SHA256Calculator.getSHA256(new File(commandLineArguments.getInput()));
//			if (ILCSQLConnection.checkIfLibAnalyzed(shasum)) {
//				return;
//			}

		Ic3Analysis analysis = new Ic3Analysis(commandLineArguments);
		analysis.performAnalysis(commandLineArguments);

	}

}
