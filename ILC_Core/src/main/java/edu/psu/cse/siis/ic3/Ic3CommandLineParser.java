//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package edu.psu.cse.siis.ic3;

import edu.psu.cse.siis.coal.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

public class Ic3CommandLineParser extends CommandLineParser<Ic3CommandLineArguments> {
    private static final String COPYRIGHT = "Copyright (C) 2015 The Pennsylvania State University and the University of Wisconsin\nSystems and Internet Infrastructure Security Laboratory\n";

    public Ic3CommandLineParser() {
    }

    protected void parseAnalysisSpecificArguments(Options options) {
        options.addOption(Option.builder("in").desc("Path to the .apk of the application.").hasArg().argName(".apk path").required().build());
        options.addOption(Option.builder("cp").desc("Path to android platforms").hasArg().argName("android platform path").required().build());
        options.addOption(Option.builder("db").desc("Store entry points to database.").hasArg().optionalArg(true).argName("DB properties file").build());
        options.addOption(Option.builder("ssh").desc("Use SSH to connect to the database.").hasArg().optionalArg(true).argName("SSH properties file").build());
        options.addOption(Option.builder("localport").desc("Local DB port to connect to.").hasArg().type(Number.class).argName("local DB port").build());
        options.addOption(Option.builder("dbhost").desc("DB host to connect to.").hasArg().type(Number.class).argName("DB host").build());
        options.addOption(Option.builder("category").desc("Category of the application").hasArg().type(Number.class).argName("App Catgorypp").build());
        options.addOption(Option.builder("dbname").desc("DB name.").hasArg().type(Number.class).argName("DB name").build());
        options.addOption("computecomponents", false, "Compute which components each exit point belongs to.");
        options.addOption("binary", false, "Output a binary protobuf.");
    }

    protected void printHelp(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        System.out.println("Copyright (C) 2015 The Pennsylvania State University and the University of Wisconsin\nSystems and Internet Infrastructure Security Laboratory\n");
        formatter.printHelp("ic3 -input <Android directory> -cp <classpath> [-computecomponents] [-db <path to DB properties file>] [-ssh <path to SSH properties file>] [-localport <DB local port>] [-modeledtypesonly] [-output <output directory>] [-dbhost DB host name/IP] [-dbname DB name][-threadcount <thread count>] [-category App Category]", options);
    }
}
