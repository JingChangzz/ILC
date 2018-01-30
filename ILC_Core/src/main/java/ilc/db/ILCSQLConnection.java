package ilc.db;

import edu.psu.cse.siis.coal.values.BasePropagationValue;
import edu.psu.cse.siis.coal.values.BottomPropagationValue;
import edu.psu.cse.siis.coal.values.PropagationValue;
import edu.psu.cse.siis.coal.values.TopPropagationValue;
import edu.psu.cse.siis.ic3.db.Constants;
import edu.psu.cse.siis.ic3.manifest.ManifestComponent;
import soot.Unit;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Administrator on 1/24/2018.
 */
public class ILCSQLConnection {

    protected static int appId = Constants.NOT_FOUND;

    protected static LibTable libTable = new LibTable();
    protected static LibraryClassesTable LibraryClassTable = new LibraryClassesTable();
    protected static ExitPointsTable exitPointsTable = new ExitPointsTable();
    protected static IntentFiltersTables intentFiltersTables = new IntentFiltersTables();

    public static void init(String dbName, String dbPropertiesPath, String sshPropertiesPath, int localDbPort) {
        DbInit.init(dbName, dbPropertiesPath, sshPropertiesPath, localDbPort);
    }

    public static void reset() {
        appId = Constants.NOT_FOUND;
    }

    public static void insertLib(String app, String version, String shasum) {
        try {
            if(appId == -1) {
                appId = libTable.insert(app, version, shasum);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static boolean checkIfAppAnalyzed(String shasum) throws SQLException {
        appId = libTable.find(shasum);

        if (appId == Constants.NOT_FOUND) {
            return false;
        }
        return true;
    }

    protected static int insertClass(String clazz) throws SQLException {
        return LibraryClassTable.insert(appId, clazz);
    }

    public static void insertIntentAtExitPoint(String className, String method,
        BasePropagationValue intentValue, String exit_kind,
        Set<String> intentPermissions, Integer missingIntents, Set<String> exitPointComponents,
        Unit unit) throws SQLException {

        PropagationValue propagationValue = null;
        if (intentValue == null || intentValue instanceof TopPropagationValue
                || intentValue instanceof BottomPropagationValue) {
            missingIntents = 0;
        } else if (intentValue instanceof PropagationValue) {
            propagationValue = (PropagationValue) intentValue;
            if (propagationValue.getPathValues() == null
                    || propagationValue.getPathValues().size() == 0) {
                missingIntents = 0;
            }
        } else {
            throw new RuntimeException("Unknown PropagationValue type: " + intentValue.getClass());
        }
        int exitPointId = insertExitPoint(className, method, exit_kind, missingIntents, unit);
//        Set<Pair<Integer, Integer>> exitPointComponentPairs = new HashSet<>();
//
//        if (exitPointComponents != null) {
//            for (String exitPointComponent : exitPointComponents) {
//                exitPointComponentPairs
//                        .add(new Pair<Integer, Integer>(exitPointId, componentToIdMap.get(exitPointComponent)));
//            }
//            try {
//                exitPointComponentTable.batchInsert(exitPointComponentPairs);
//            } catch (Exception e) {
//
//            }
//        }
//        if (missingIntents == null) {
//            Set<PathValue> singleIntentValues = propagationValue.getPathValues();
//            if (singleIntentValues != null) {
//                for (PathValue singleIntentValue : singleIntentValues) {
//                    if (exit_kind.equals(Constants.ComponentShortType.PROVIDER)) {
//                        insertUriAndValue(exitPointId, singleIntentValue);
//                    } else {
//                        int intentId = insertIntentAndValue(exitPointId, singleIntentValue);
//                    }
//                }
//            }
//        }



    }

    protected static int insertExitPoint(String className, String method, String exit_kind, Integer missingIntentFilters, Unit unit) throws SQLException {
        int classId = insertClass(className);
        return exitPointsTable.insert(classId, method, exit_kind, missingIntentFilters,
                unit.toString());
    }

    public static void insertIntentFilters(List<ManifestComponent> components) {
        if (appId == Constants.NOT_FOUND) {
            throw new RuntimeException("appId has not been set");
        }
        Map<String, Integer> componentIds = new HashMap<String, Integer>();
//        for (ManifestComponent component : components) {
//            int componentId;
//            if (component instanceof ManifestProviderComponent) {
//                componentId = insertProvider((ManifestProviderComponent) component);
//                componentIds.put(component.getName(), componentId);
//                continue;
//            }
//            componentId = insertComponent(component.getName(), component.getType(),
//                    component.isExported(), component.getPermission(), component.missingIntentFilters());
//            componentIds.put(component.getName(), componentId);
//            Set<ManifestIntentFilter> intentFilters = component.getIntentFilters();
//            System.out.println("Inserting " + intentFilters);
//            if (intentFilters != null) {
//                for (ManifestIntentFilter intentFilter : component.getIntentFilters()) {
//                    //存入IntentFilters表。action和cacategory可能会有多个！！！
//                    intentFiltersTables.insertS()
//                }
//            }
//        }

    }
}
