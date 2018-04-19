package edu.psu.cse.siis.ic3;

import edu.psu.cse.siis.ic3.db.SQLConnection;
import ilc.db.ILCSQLConnection;
import ilc.main.Core;

import java.sql.SQLException;
import java.util.Set;

public class Ic3Main {
	public static String manifest;
	public static String arscFile;
	public static String resDir;
	public static Set<String> entryPointClasses;
	public static boolean isPlainEn;
	public static void main(String[] args, String manifestPath, String arsc, String resD, boolean isPlain) throws SQLException {
		edu.psu.cse.siis.coal.Main.reset();
		SQLConnection.reset();
		SQLConnection.appId = Core.appID;
		manifest = manifestPath;
		arscFile = arsc;
		resDir = resD;
		if (isPlain){
			entryPointClasses = Core.parseJar.plainEntryPoints;
		}else {
			entryPointClasses = Core.parseJar.entryPointsForAndroid;
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
		edu.psu.cse.siis.coal.Main.reset();
	}

}
