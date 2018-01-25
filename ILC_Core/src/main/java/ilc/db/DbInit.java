package ilc.db;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import edu.psu.cse.siis.ic3.db.SQLConnection;

import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import static com.sun.org.apache.xerces.internal.utils.SecuritySupport.getResourceAsStream;

/**
 * Created by Administrator on 1/24/2018.
 */
public class DbInit {
    private static final int MYSQL_PORT = 3306;
    private static String url = null;
    private static Session session = null;
    private static Connection connection = null;
    private static String sshPropertiesPath;
    private static String dbPropertiesPath;
    private static String dbHost = "localhost";
    private static int localPort;

    protected PreparedStatement insertStatement = null;
    protected PreparedStatement findStatement = null;
    private PreparedStatement selectLastInsertId = null;

    public static void init(String dbName, String dbPropertiesPath, String sshPropertiesPath,
                            int localPort) {
        DbInit.sshPropertiesPath = sshPropertiesPath;
        DbInit.dbPropertiesPath = dbPropertiesPath;
        DbInit.localPort = localPort;
        url =  "jdbc:mysql://" + dbHost + ":" + localPort + "/" + dbName;
    }

    public static Connection getConnection() {
        connect();
        return connection;
    }

    private static void connect() {
        if (url == null) {
            throw new RuntimeException(
                    "Method init() should be called first to initialize database connection");
        }

        if (sshPropertiesPath != null) {
            try {
                makeSshTunnel();
            } catch (NumberFormatException | IOException | JSchException e) {
                e.printStackTrace();
                return;
            }
        }

        try {
            if (connection != null && !connection.isClosed()) {
                return;
            }
        } catch (SQLException e2) {
            e2.printStackTrace();
        }

        Properties properties = new Properties();

        try {
            if (dbPropertiesPath.startsWith("/db/")) {
                properties.load(SQLConnection.class.getResourceAsStream(dbPropertiesPath));
            } else {
                properties.load(new FileReader(dbPropertiesPath));
            }
        } catch (IOException e1) {
            e1.printStackTrace();
            return;
        }

        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            e.printStackTrace();
            return;
        }

        try {

            String userName=(String)properties.get("user");
            String password=(String)properties.get("password");

            url=url+"?user="+userName+"&password="+password+"&useSSL=false";
            //System.out.println(url);

            connection = DriverManager.getConnection(url);
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }
    }

    private static void makeSshTunnel() throws IOException, NumberFormatException, JSchException {
        if (session != null && session.isConnected()) {
            return;
        }

        Properties sshProperties = new Properties();
        if (sshPropertiesPath.startsWith("/db/")) {
            sshProperties.load(getResourceAsStream(sshPropertiesPath));
        } else {
            sshProperties.load(new FileReader(sshPropertiesPath));
        }

        JSch jSch = new JSch();
        String host = sshProperties.getProperty("host");
        session = jSch.getSession(sshProperties.getProperty("user"), host,
                Integer.valueOf(sshProperties.getProperty("port")));
        session.setConfig("StrictHostKeyChecking", "no");
        jSch.addIdentity(sshProperties.getProperty("identity"));
        session.connect();
        session.setPortForwardingL(localPort, host, MYSQL_PORT);
    }

    public static void setDBHost(String hostName){
        dbHost=hostName;
    }

    protected int findAutoIncrement() throws SQLException {
        connect();
        if(this.selectLastInsertId == null || this.selectLastInsertId.isClosed()) {
            this.selectLastInsertId = connection.prepareStatement("SELECT LAST_INSERT_ID()");
        }

        ResultSet resultSet = this.selectLastInsertId.executeQuery();
        int autoinc;
        if(resultSet.next()) {
            autoinc = resultSet.getInt("LAST_INSERT_ID()");
        } else {
            autoinc = -1;
        }

        resultSet.close();
        return autoinc;
    }
}
