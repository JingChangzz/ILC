package ilc.db;

import edu.psu.cse.siis.ic3.db.Table;

import java.sql.SQLException;

/**
 * Created by Administrator on 1/30/2018.
 */
public class ICCEntryLeaksTable extends Table{
    private static final String INSERT = "INSERT INTO ICCEntryLeaks "
            + "(entry_point_id, leak_source,leak_sink, leak_path, sink_method, trigger_method) VALUES (?, ?,?, ?, ?, ?)";

  /*
   * private static final String FIND =
   * "SELECT id FROM ExitPoints WHERE class_id = ? AND method = ? AND instruction = ? " +
   * "AND exit_kind = ?";
   */

    public int insert(int entryPoint, String leakSource, String leakSink, String leakPath, String sink_method, String trigger_api)
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
        if (leakSink.length() > 512) {
            leakSink = leakPath.substring(0, 512);
        }
        if (sink_method.length() > 127){
            sink_method = sink_method.substring(0, 127);
        }
        if (trigger_api.length() > 127){
            trigger_api = trigger_api.substring(0, 127);
        }
        insertStatement.setString(2, leakSource);
        insertStatement.setString(4, leakPath);
        insertStatement.setString(3, leakSink);
        insertStatement.setString(5, sink_method);
        insertStatement.setString(6, trigger_api);

        if (insertStatement.executeUpdate() == 0) {
            return NOT_FOUND;
        }
        return findAutoIncrement();
    }
}
