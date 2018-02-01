package ilc.db;

import edu.psu.cse.siis.ic3.db.Table;

import java.sql.SQLException;

/**
 * Created by Administrator on 1/30/2018.
 */
public class ICCEntryLeaksTable extends Table{
    private static final String INSERT = "INSERT INTO ICCEntryLeaks "
            + "(entry_point_id, leak_source,leak_sink, leak_path) VALUES (?, ?,?, ?)";

  /*
   * private static final String FIND =
   * "SELECT id FROM ExitPoints WHERE class_id = ? AND method = ? AND instruction = ? " +
   * "AND exit_kind = ?";
   */

    public int insert(int entryPoint, String leakSource, String leakSink, String leakPath)
            throws SQLException {
        // int id = find(classId, method, instruction, exit_kind);
        // if (id != NOT_FOUND) {
        // return id;
        // }
        if (insertStatement == null || insertStatement.isClosed()) {
            insertStatement = getConnection().prepareStatement(INSERT);
        }
        insertStatement.setInt(1, entryPoint);
        if (leakSource.length() > 512) {
            leakSource = leakSource.substring(0, 512);
        }
        insertStatement.setString(2, leakSource);
        insertStatement.setString(4, leakPath);
        insertStatement.setString(3, leakSink);

        if (insertStatement.executeUpdate() == 0) {
            return NOT_FOUND;
        }
        return findAutoIncrement();
    }
}
