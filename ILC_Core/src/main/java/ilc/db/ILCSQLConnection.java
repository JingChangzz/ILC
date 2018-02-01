package ilc.db;

import edu.psu.cse.siis.ic3.db.SQLConnection;
import ilc.data.PermissionAnalysis;
import soot.Unit;

import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Created by Administrator on 1/24/2018.
 */
public class ILCSQLConnection extends SQLConnection{
    protected static ICCExitLeaksTable dataExitLeaksTable = new ICCExitLeaksTable();
    protected static EntryPointsTable entryPointsTable = new EntryPointsTable();
    protected static ICCEntryLeaksTable dataEntryLeaksTable = new ICCEntryLeaksTable();
    protected static PermissionLeakTable permissionLeakTable = new PermissionLeakTable();
    protected static SourceSinkCountTable sourceSinkCountTable = new SourceSinkCountTable();

    public static void insertDataExitLeak(String className, String method, int instruction, Unit unit,
                                          String source, String sink, String path, String methodCalling) throws SQLException {

        String exit_kind = "a";
        if (sink.contains("startService") || sink.contains("bindService")) {
            exit_kind = "s";
        } else if (sink.contains("sendBroadcast") || sink.contains("sendOrderedBroadcast")
                || sink.contains("sendStickyBroadcast") || sink.contains("sendStickyOrderedBroadcast")) {
            exit_kind = "r";
        } else if (sink.contains("ContentResolver")||sink.contains("android.database")) {
            exit_kind = "p";
        } else if (sink.contains("write")){  //写文件
            exit_kind = "f";
        }

        int exitPointID = insertExitPoint(className, method, instruction, exit_kind, 0, unit);
        ArrayList<String> permissions = PermissionAnalysis.getPermissionList(source);
        int leakID = dataExitLeaksTable.insert(exitPointID, source, sink, path, methodCalling);

        for (String permission : permissions) {
            int permissionId = permissionStringTable.insert(permission);
            permissionLeakTable.insert(leakID, permissionId);
        }
    }

    public static void insertDataEntryLeak(String className, String method, int instruction,
                                             Unit unit, String source, String sink, String path) throws SQLException {
        int classId = insertClass(className);
        int entryPointID = entryPointsTable.insert(classId, method, instruction, source);
        dataEntryLeaksTable.insert(entryPointID, source, sink, path);
    }

    protected static int insertExitPoint(String className, String method, int instruction,
                                         String exit_kind, Integer missingIntentFilters, Unit unit) throws SQLException {
        int classId = insertClass(className);
        return exitPointTable.insert(classId, method, instruction, exit_kind, missingIntentFilters,
                unit.toString());
    }

}
