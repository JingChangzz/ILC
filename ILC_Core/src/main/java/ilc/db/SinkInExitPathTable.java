package ilc.db;

import edu.psu.cse.siis.ic3.db.Table;

import java.sql.SQLException;

/**
 * Created by Administrator on 3/24/2018.
 */
public class SinkInExitPathTable extends Table {
    private static final String INSERT = "INSERT INTO SinkInExitPath "
            + "(AppID, trigger_api, sink, in_method) VALUES (?, ?, ?, ?)";

    public int insert(int appid, String trigger, String in_method, String source) throws SQLException {
        // int id = find(classId, method, instruction, exit_kind);
        // if (id != NOT_FOUND) {
        // return id;
        // }
        if (insertStatement == null || insertStatement.isClosed()) {
            insertStatement = getConnection().prepareStatement(INSERT);
        }
        insertStatement.setInt(1, appid);
        insertStatement.setString(2, trigger);
        insertStatement.setString(3, source);
        insertStatement.setString(4, in_method);

        if (insertStatement.executeUpdate() == 0) {
            return NOT_FOUND;
        }
        return findAutoIncrement();
    }

}
