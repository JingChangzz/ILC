package ilc.db;

import edu.psu.cse.siis.ic3.db.Table;

import java.sql.SQLException;

/**
 * Created by Administrator on 3/24/2018.
 */
public class LibTotalAnalysisTime extends Table {
    private static final String INSERT =
            "INSERT INTO LibAnalysisTime (`AppID`, `TotalTime`, `ClassNum`, `MethodNum`) VALUES (?, ?, ?, ?);";
    private static final String FIND = "SELECT id FROM AppAnalysisTime WHERE AppID = ?";
    private static final String DELETE = "DELETE FROM AppAnalysisTime WHERE AppID = ?";

    public int insert(int app, long totalTime, long classNum, long methodNum) throws SQLException {
        delete(app);

        insertStatement = getConnection().prepareStatement(INSERT);

        insertStatement.setInt(1, app);
        insertStatement.setLong(2, totalTime);
        insertStatement.setLong(3, classNum);
        insertStatement.setLong(4, methodNum);

        if (insertStatement.executeUpdate() == 0) {
            return NOT_FOUND;
        }

        return findAutoIncrement();
    }

    public void delete(int app) throws SQLException {
        findStatement = getConnection().prepareStatement(DELETE);
        findStatement.setInt(1, app);

        findStatement.executeUpdate();
        findStatement.close();

    }
}
