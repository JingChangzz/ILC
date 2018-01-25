package ilc.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by Administrator on 1/24/2018.
 */
public class LibTable extends DbInit{
    private static final String INSERT = "INSERT INTO Applications (app, version,shasum) VALUES (?, ?,?)";
    private static final String FIND = "SELECT id FROM Applications WHERE shasum = ?";

    public int insert(String app, int version, String shasum) throws SQLException {
        int id = find(shasum);
        findStatement.close();
        return id != -1?id:forceInsert(app, version, shasum);
    }

    public int find(String shasum) throws SQLException {
        findStatement = getConnection().prepareStatement("SELECT id FROM Applications WHERE shasum = ?");
        findStatement.setString(1, shasum);
        return processIntFindQuery(findStatement);
    }

    public int forceInsert(String app, int version, String shasum) throws SQLException {
        if(this.insertStatement == null || this.insertStatement.isClosed()) {
            this.insertStatement = getConnection().prepareStatement("INSERT INTO Applications (app, version,shasum) VALUES (?, ?,?)");
        }

        this.insertStatement.setString(1, app);
        if(version == -1) {
            this.insertStatement.setNull(2, 4);
        } else {
            this.insertStatement.setInt(2, version);
        }

        this.insertStatement.setString(3, shasum);
        return this.insertStatement.executeUpdate() == 0?-1:this.findAutoIncrement();
    }

    protected int processIntFindQuery(PreparedStatement statement) throws SQLException {
        return this.processIntFindQuery(statement, "id");
    }

    protected int processIntFindQuery(PreparedStatement statement, String column) throws SQLException {
        ResultSet resultSet = statement.executeQuery();
        int result;
        if(resultSet.next()) {
            result = resultSet.getInt(column);
        } else {
            result = -1;
        }

        resultSet.close();
        return result;
    }


}
