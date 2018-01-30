package ilc.db;

import java.sql.SQLException;
import java.sql.Types;

/**
 * Created by Administrator on 1/27/2018.
 */
public class ExitPointsTable extends DbInit {
    private static final String INSERT = "INSERT INTO ExitPoints "
            + "(class_id, method, statement, exit_kind, missing) VALUES (?, ?, ?, ?, ?,?)";

    private static final String FIND =
            "SELECT id FROM ExitPoints WHERE class_id = ? AND method = ? AND  statement = ?"
                    + "AND exit_kind = ?";

    public int insert(int classId, String method, String exit_kind,
                      Integer missingIntents, String unit) throws SQLException {
        int id = find(classId, method, unit, exit_kind);
        if (id != NOT_FOUND) {
            return id;
        }
        if (insertStatement == null || insertStatement.isClosed()) {
            insertStatement = getConnection().prepareStatement(INSERT);
        }
        insertStatement.setInt(1, classId);
        if (method.length() > 512) {
            method = method.substring(0, 512);
        }
        insertStatement.setString(2, method);
        insertStatement.setString(3, unit);
        insertStatement.setString(4, exit_kind);
        if (missingIntents == null) {
            insertStatement.setNull(5, Types.INTEGER);
        } else {
            insertStatement.setInt(5, missingIntents);
        }

        if (insertStatement.executeUpdate() == 0) {
            return NOT_FOUND;
        }
        return findAutoIncrement();
    }

    public int find(int classId, String method, String unit, String exit_kind)
            throws SQLException {
        if (findStatement == null || findStatement.isClosed()) {
            findStatement = getConnection().prepareStatement(FIND);
        }
        findStatement.setInt(1, classId);
        findStatement.setString(2, method);
        findStatement.setString(3, unit);
        findStatement.setString(4, exit_kind);
        return processIntFindQuery(findStatement);
    }

}
