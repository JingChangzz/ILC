package ilc.db;

import java.sql.SQLException;

/**
 * Created by Administrator on 1/27/2018.
 */
public class LibraryClassesTable extends DbInit{
    private static final String INSERT = "INSERT INTO %s (%s, %s) VALUES (?, ?)";
    private static final String FIND = "SELECT id FROM %s WHERE %s = ? AND %s = ?";

    public LibraryClassesTable(){
        insertString = String.format(INSERT, "libraryclasses", "library_id", "class");
        findString = String.format(FIND, "libraryclasses", "library_id", "class");
    }

    public int insert(int libraryId, String clazz) throws SQLException {
        int id = find(libraryId, clazz);
        if (id != NOT_FOUND) {
            return id;
        }

        return forceInsert(libraryId, clazz);
    }

    public int forceInsert(int firstValue, String secondValue) throws SQLException {
        if (insertStatement == null || insertStatement.isClosed()) {
            insertStatement = getConnection().prepareStatement(insertString);
        }
        if (secondValue == null) {
            secondValue = Constants.NULL_STRING;
        }
        insertStatement.setInt(1, firstValue);
        insertStatement.setString(2, secondValue);
        if (insertStatement.executeUpdate() == 0) {
            return NOT_FOUND;
        }
        return findAutoIncrement();
    }

    private int find(int libraryId, String clazz) throws SQLException {
        if (findStatement == null || findStatement.isClosed()) {
            findStatement = getConnection().prepareStatement(findString);
        }
        findStatement.setInt(1, libraryId);
        findStatement.setString(2, clazz);
        return processIntFindQuery(findStatement);
    }

}
