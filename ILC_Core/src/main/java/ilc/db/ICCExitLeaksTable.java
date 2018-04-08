package ilc.db;

import edu.psu.cse.siis.ic3.db.Table;

import java.sql.SQLException;

/**
 * Created by Administrator on 1/30/2018.
 */
public class ICCExitLeaksTable extends Table{
    private static final String INSERT = "INSERT INTO ICCExitLeaks "
            + "(exit_point_id, leak_source, leak_path, leak_sink, method, trigger_method) VALUES (?, ?, ?, ?, ?, ?)";

  /*
   * private static final String FIND =
   * "SELECT id FROM ExitPoints WHERE class_id = ? AND method = ? AND instruction = ? " +
   * "AND exit_kind = ?";
   */

    public int insert(int exitPoint, String leakSource, String leakSink, String leakPath,
                      String method, String trigger) throws SQLException {
        // int id = find(classId, method, instruction, exit_kind);
        // if (id != NOT_FOUND) {
        // return id;
        // }
        if (insertStatement == null || insertStatement.isClosed()) {
            insertStatement = getConnection().prepareStatement(INSERT);
        }
        insertStatement.setInt(1, exitPoint);
        if (leakSource.length() > 1024) {
            leakSource = leakSource.substring(0, 1024);
        }
        if (leakSink.length() > 1024) {
            leakSink = leakSink.substring(0, 1024);
        }
        insertStatement.setString(2, leakSource);
        insertStatement.setString(3, leakPath);
        insertStatement.setString(4, leakSink);
        insertStatement.setString(5, method);
        insertStatement.setString(6, trigger);

        if (insertStatement.executeUpdate() == 0) {
            return NOT_FOUND;
        }
        return findAutoIncrement();
    }
}
