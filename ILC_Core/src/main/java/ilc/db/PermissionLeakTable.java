package ilc.db;

import edu.psu.cse.siis.ic3.db.TwoIntTable;

import java.sql.SQLException;

/**
 * Created by Administrator on 1/30/2018.
 */
public class PermissionLeakTable extends TwoIntTable {

    PermissionLeakTable() {
        super("PermissionLeaks", "ICCLeakID", "PermissionID");
    }

    public int insert(int ICCLeakID, int PermissionID) throws SQLException {
        return super.insert(ICCLeakID, PermissionID);
    }

    public int forceInsert(int ICCLeakID, int PermissionID) throws SQLException {
        return super.forceInsert(ICCLeakID, PermissionID);
    }

    public int find(int ICCLeakID, int PermissionID) throws SQLException {
        return super.find(ICCLeakID, PermissionID);
    }
}
