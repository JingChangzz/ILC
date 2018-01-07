//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package edu.psu.cse.siis.ic3;

import edu.psu.cse.siis.coal.CommandLineArguments;
import edu.psu.cse.siis.ic3.db.Table;
import org.apache.commons.cli.ParseException;

public class Ic3CommandLineArguments extends CommandLineArguments {
    private static final String DEFAULT_SSH_PROPERTIES_PATH = "/db/ssh.properties";
    private static final String DEFAULT_DATABASE_PROPERTIES_PATH = "cc.properties";
    private static final int DEFAULT_LOCAL_PORT = 3306;
    private static final String DEFAULT_COMPILED_MODEL_PATH = "/res/icc.cmodel";
    private static final String DEFAULT_DB_NAME = "dialdroid";
    private static final String DEFAULT_DB_HOST_NAME = "localhost";
    private String manifest;
    private String db;
    private String ssh;
    private String iccStudy;
    private int dbLocalPort = 3306;
    private final boolean computeComponents = true;
    private String dbName;
    private String protobufDestination;
    private boolean binary;
    private String sample;
    private String appCategory = "Default";
    private String dbHostName;

    public Ic3CommandLineArguments() {
    }

    public String getDbName() {
        return this.dbName != null?this.dbName:"dialdroid";
    }

    public String getManifest() {
        return this.manifest;
    }

    public String getDb() {
        return this.db == null?"cc.properties":this.db;
    }

    public String getSsh() {
        return this.ssh;
    }

    public String getIccStudy() {
        return this.iccStudy;
    }

    public int getDbLocalPort() {
        return this.dbLocalPort;
    }

    public boolean computeComponents() {
        return true;
    }

    public String getProtobufDestination() {
        return null;
    }

    public boolean binary() {
        return this.binary;
    }

    public String getSample() {
        return this.sample;
    }

    public String getAppCategory() {
        return this.appCategory;
    }

    public void processCommandLineArguments() {
        this.manifest = this.getOptionValue("in");
        if(this.getCompiledModel() == null && this.getModel() == null) {
            this.setCompiledModel("/res/icc.cmodel");
        }

        this.iccStudy = this.getOptionValue("iccstudy");
        if(this.hasOption("db")) {
            this.db = this.getOptionValue("db", "cc.properties");
        }

        if(this.hasOption("dbhost")) {
            this.dbHostName = this.getOptionValue("dbhost", "localhost");
            Table.setDBHost(this.dbHostName);
        }

        if(this.hasOption("cp")) {
            this.setAndroidJar(this.getOptionValue("cp"));
        }

        if(this.hasOption("ssh")) {
            this.ssh = this.getOptionValue("ssh", "/db/ssh.properties");
        }

        if(this.hasOption("localport")) {
            try {
                this.dbLocalPort = ((Number)this.getParsedOptionValue("localport")).intValue();
            } catch (ParseException var2) {
                var2.printStackTrace();
            }
        }

        if(this.hasOption("dbname")) {
            this.dbName = this.getOptionValue("dbname", "dialdroid");
        }

        if(this.hasOption("category")) {
            this.appCategory = this.getOptionValue("category", "Default");
        }

        if(this.hasOption("protobuf")) {
            this.protobufDestination = this.getOptionValue("protobuf");
        }

    }
}
