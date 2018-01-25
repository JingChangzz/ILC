package ilc.db;

import edu.psu.cse.siis.ic3.db.Constants;

import java.sql.SQLException;

/**
 * Created by Administrator on 1/24/2018.
 */
public class ILCSQLConnection {

    protected static int appId = Constants.NOT_FOUND;

    protected static LibTable libTable = new LibTable();

    public static void init(String dbName, String dbPropertiesPath, String sshPropertiesPath, int localDbPort) {
        DbInit.init(dbName, dbPropertiesPath, sshPropertiesPath, localDbPort);
    }

    public static void reset() {
        appId = Constants.NOT_FOUND;
    }

    public static void insertLib(String app, int version, String shasum) {
        try {
            if(appId == -1) {
                appId = libTable.insert(app, version, shasum);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
