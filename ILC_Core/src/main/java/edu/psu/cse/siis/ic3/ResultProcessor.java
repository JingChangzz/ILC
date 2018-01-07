//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package edu.psu.cse.siis.ic3;

import edu.psu.cse.siis.coal.AnalysisParameters;
import edu.psu.cse.siis.coal.Model;
import edu.psu.cse.siis.coal.PropagationTimers;
import edu.psu.cse.siis.coal.Result;
import edu.psu.cse.siis.coal.Results;
import edu.psu.cse.siis.coal.arguments.Argument;
import edu.psu.cse.siis.coal.field.values.FieldValue;
import edu.psu.cse.siis.coal.field.values.ScalarFieldValue;
import edu.psu.cse.siis.coal.field.values.TopFieldValue;
import edu.psu.cse.siis.coal.values.BasePropagationValue;
import edu.psu.cse.siis.coal.values.BottomPropagationValue;
import edu.psu.cse.siis.coal.values.PathValue;
import edu.psu.cse.siis.coal.values.PropagationValue;
import edu.psu.cse.siis.coal.values.TopPropagationValue;
import edu.psu.cse.siis.ic3.db.DbConnection;
import edu.psu.cse.siis.ic3.db.SQLConnection;
import edu.psu.cse.siis.ic3.manifest.ManifestComponent;
import edu.psu.cse.siis.ic3.manifest.ManifestData;
import edu.psu.cse.siis.ic3.manifest.ManifestIntentFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import soot.Scene;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;
import soot.tagkit.LineNumberTag;

import java.io.IOException;
import java.io.Writer;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class ResultProcessor {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final String ENTRY_POINT_INTENT = "<INTENT>";
    private final int[] preciseNonLinking = new int[]{0, 0, 0, 0};
    private final int[] preciseLinking = new int[]{0, 0, 0, 0};
    private final int[] imprecise = new int[]{0, 0, 0, 0, 0};
    private final int[] top = new int[]{0, 0, 0};
    private final int[] bottom = new int[]{0, 0, 0};
    private final int[] nonexistent = new int[]{0, 0, 0};
    private final int[] preciseFieldValueCount = new int[]{0, 0, 0};
    private final int[] partiallyPreciseFieldValueCount = new int[]{0, 0, 0};
    private final int[] impreciseFieldValueCount = new int[]{0, 0, 0};
    private int intentWithData = 0;
    private int providerArgument = 0;

    public ResultProcessor() {
    }

    public void processResult(boolean writeToDb, String appName, Map<String, Integer> componentToIdMap, int analysisClassesCount, Writer writer) throws IOException, SQLException {
        Iterator statistics = Results.getResults().iterator();

        while(statistics.hasNext()) {
            Result result = (Result)statistics.next();
            ((Ic3Result)result).dump();
            this.analyzeResult(result);
            if(writeToDb) {
                this.writeResultToDb(result, componentToIdMap);
            }
        }

        if(writeToDb) {
            SQLConnection.closeConnection();
        }

        Timers.v().totalTimer.end();
        String statistics1 = appName + " " + analysisClassesCount + " " + PropagationTimers.v().reachableMethods + " " + PropagationTimers.v().reachableStatements + " " + this.preciseNonLinking[0] + " " + this.preciseNonLinking[3] + " " + this.preciseNonLinking[1] + " " + this.preciseNonLinking[2] + " " + this.preciseLinking[0] + " " + this.preciseLinking[3] + " " + this.preciseLinking[1] + " " + this.preciseLinking[2] + " " + this.imprecise[0] + " " + this.imprecise[3] + " " + this.imprecise[1] + " " + this.imprecise[2] + " " + this.bottom[0] + " " + this.bottom[1] + " " + this.bottom[2] + " " + this.top[0] + " " + this.top[1] + " " + this.top[2] + " " + this.nonexistent[0] + " " + this.nonexistent[1] + " " + this.nonexistent[2] + " " + this.providerArgument + " " + this.imprecise[4] + " " + PropagationTimers.v().pathValues + " " + PropagationTimers.v().separatePathValues + " " + PropagationTimers.v().modelParsing.getTime() + " " + Timers.v().mainGeneration.getTime() + " " + Timers.v().entryPointMapping.getTime() + " " + Timers.v().classLoading.getTime() + " " + PropagationTimers.v().problemGeneration.getTime() + " " + PropagationTimers.v().ideSolution.getTime() + " " + PropagationTimers.v().valueComposition.getTime() + " " + PropagationTimers.v().resultGeneration.getTime() + " " + (PropagationTimers.v().soot.getTime() - PropagationTimers.v().totalTimer.getTime()) + " " + (Timers.v().misc.getTime() + PropagationTimers.v().misc.getTime()) + " " + Timers.v().totalTimer.getTime() + "\n";
        if(this.logger.isInfoEnabled()) {
            this.logger.info(statistics1);
        }

        if(writer != null) {
            writer.write(statistics1);
            writer.close();
        }

    }

    private void writeResultToDb(Result result, Map<String, Integer> componentToIdMap) throws SQLException {
        Iterator var3 = result.getResults().entrySet().iterator();

        while(true) {
            while(true) {
                Entry entry;
                Unit unit;
                Argument[] arguments;
                Map entryPointMap;
                do {
                    if(!var3.hasNext()) {
                        return;
                    }

                    entry = (Entry)var3.next();
                    unit = (Unit)entry.getKey();
                    arguments = Model.v().getArgumentsForQuery((Stmt)unit);
                    entryPointMap = ((Ic3Result)result).getEntryPointMap();
                } while(arguments == null);

                SootMethod method = AnalysisParameters.v().getIcfg().getMethodOf(unit);
                int unitId = this.getIdForUnit(unit, method);
                if(unit.hasTag("LineNumberTag")) {
                    unitId = ((LineNumberTag)unit.getTag("LineNumberTag")).getLineNumber();
                }

                HashMap valueMap = new HashMap(arguments.length);
                Map argnumToValueMap = (Map)entry.getValue();
                Argument[] className = arguments;
                int methodSignature = arguments.length;

                for(int basePropagationValue = 0; basePropagationValue < methodSignature; ++basePropagationValue) {
                    Argument targetType = className[basePropagationValue];
                    valueMap.put(targetType.getProperty("valueType"), argnumToValueMap.get(Integer.valueOf(targetType.getArgnum()[0])));
                }

                String var19 = method.getDeclaringClass().getName();
                String var20 = method.getSignature();
                if(valueMap.containsKey("activity")) {
                    DbConnection.insertIntentAtExitPoint(var19, var20, unitId, (BasePropagationValue)valueMap.get("activity"), "a", (Set)null, (Integer)null, (Set)entryPointMap.get(method), componentToIdMap, unit);
                } else if(valueMap.containsKey("service")) {
                    DbConnection.insertIntentAtExitPoint(var19, var20, unitId, (BasePropagationValue)valueMap.get("service"), "s", (Set)null, (Integer)null, (Set)entryPointMap.get(method), componentToIdMap, unit);
                } else if(valueMap.containsKey("receiver")) {
                    DbConnection.insertIntentAtExitPoint(var19, var20, unitId, (BasePropagationValue)valueMap.get("receiver"), "r", (Set)valueMap.get("permission"), (Integer)null, (Set)entryPointMap.get(method), componentToIdMap, unit);
                } else if(valueMap.containsKey("intentFilter")) {
                    this.insertDynamicReceiver((Set)valueMap.get("permission"), (Set)valueMap.get("receiverType"), (BasePropagationValue)valueMap.get("intentFilter"), method, unit);
                } else if(valueMap.containsKey("provider")) {
                    DbConnection.insertIntentAtExitPoint(var19, var20, unitId, (BasePropagationValue)valueMap.get("provider"), "p", (Set)null, (Integer)null, (Set)entryPointMap.get(method), componentToIdMap, unit);
                } else if(valueMap.containsKey("authority")) {
                    DbConnection.insertIntentAtExitPoint(var19, var20, unitId, this.getUriValueForAuthorities((Set)valueMap.get("authority")), "p", (Set)null, (Integer)null, (Set)entryPointMap.get(method), componentToIdMap, unit);
                } else if(valueMap.containsKey("pendingIntent")) {
                    BasePropagationValue var21 = (BasePropagationValue)valueMap.get("pendingIntent");
                    String var22 = var21 instanceof PropagationValue?(String)((FieldValue)((PropagationValue)var21).getValuesForField("targetType").iterator().next()).getValue():null;
                    Set permissions = (Set)valueMap.get("permission");
                    if(var22 != null) {
                        DbConnection.insertIntentAtExitPoint(var19, var20, unitId, var21, var22, permissions, (Integer)null, (Set)entryPointMap.get(method), componentToIdMap, unit);
                    } else {
                        Iterator var17 = Arrays.asList(new String[]{"a", "r", "s"}).iterator();

                        while(var17.hasNext()) {
                            String target = (String)var17.next();
                            DbConnection.insertIntentAtExitPoint(var19, var20, unitId, var21, target, permissions, (Integer)null, (Set)entryPointMap.get(method), componentToIdMap, unit);
                        }
                    }
                } else if(valueMap.containsKey("componentExtra")) {
                    DbConnection.insertComponentExtras((Set)entryPointMap.get(method), componentToIdMap, (Set)valueMap.get("componentExtra"));
                }
            }
        }
    }

    private void insertDynamicReceiver(Set<String> permissions, Set<String> receiverTypes, BasePropagationValue intentFilters, SootMethod method, Unit unit) throws SQLException {
        if(permissions == null) {
            permissions = Collections.singleton(null);
        }

        Iterator var6 = receiverTypes.iterator();

        while(var6.hasNext()) {
            String receiverType = (String)var6.next();
            Iterator var8 = permissions.iterator();

            while(var8.hasNext()) {
                String permission = (String)var8.next();
                this.insertDynamicReceiverHelper(permission, receiverType, intentFilters, method, unit);
            }
        }

    }

    private void insertDynamicReceiverHelper(String permission, String receiverType, BasePropagationValue intentFilters, SootMethod method, Unit unit) throws SQLException {
        Integer missingIntentFilters;
        HashSet manifestIntentFilters;
        if(intentFilters != null && !(intentFilters instanceof TopPropagationValue) && !(intentFilters instanceof BottomPropagationValue)) {
            if(!(intentFilters instanceof PropagationValue)) {
                throw new RuntimeException("Unknown intent filter type: " + intentFilters.getClass());
            }

            missingIntentFilters = null;
            PropagationValue manifestComponent = (PropagationValue)intentFilters;
            manifestIntentFilters = new HashSet();

            PathValue branchValue;
            Integer filterPriority;
            for(Iterator var9 = manifestComponent.getPathValues().iterator(); var9.hasNext(); manifestIntentFilters.add(new ManifestIntentFilter(branchValue.getSetStringFieldValue("actions"), branchValue.getSetStringFieldValue("categories"), false, this.makeManifestData(branchValue), filterPriority))) {
                branchValue = (PathValue)var9.next();
                filterPriority = null;
                FieldValue priorityFieldValue = branchValue.getFieldValue("priority");
                if(priorityFieldValue != null) {
                    filterPriority = (Integer)priorityFieldValue.getValue();
                }
            }
        } else {
            missingIntentFilters = Integer.valueOf(0);
            manifestIntentFilters = null;
        }

        ManifestComponent manifestComponent1 = new ManifestComponent("r", receiverType, true, true, permission, (String)null, missingIntentFilters, method, unit);
        manifestComponent1.setIntentFilters(manifestIntentFilters);
        SQLConnection.insertIntentFilters(Collections.singletonList(manifestComponent1));
    }

    private List<ManifestData> makeManifestData(PathValue branchValue) {
        Set mimeTypes = branchValue.getSetStringFieldValue("dataType");
        Set authorities = branchValue.getSetFieldValue("authorities", DataAuthority.class);
        Set paths = branchValue.getSetStringFieldValue("paths");
        Set schemes = branchValue.getSetStringFieldValue("schemes");
        if(mimeTypes == null && authorities == null && paths == null && schemes == null) {
            return null;
        } else {
            if(mimeTypes == null) {
                mimeTypes = Collections.singleton((Object)null);
            }

            if(authorities == null) {
                authorities = Collections.singleton(new DataAuthority((String)null, (String)null));
            }

            if(paths == null) {
                paths = Collections.singleton((Object)null);
            }

            if(schemes == null) {
                schemes = Collections.singleton((Object)null);
            }

            ArrayList result = new ArrayList();
            Iterator var7 = mimeTypes.iterator();

            while(var7.hasNext()) {
                String mimeType = (String)var7.next();
                Iterator var9 = authorities.iterator();

                while(var9.hasNext()) {
                    DataAuthority dataAuthority = (DataAuthority)var9.next();
                    Iterator var11 = paths.iterator();

                    while(var11.hasNext()) {
                        String dataPath = (String)var11.next();
                        Iterator var13 = schemes.iterator();

                        while(var13.hasNext()) {
                            String scheme = (String)var13.next();
                            result.add(new ManifestData(scheme, dataAuthority.getHost(), dataAuthority.getPort(), dataPath, mimeType));
                        }
                    }
                }
            }

            return result;
        }
    }

    private BasePropagationValue getUriValueForAuthorities(Set<String> authorities) {
        if(authorities == null) {
            return null;
        } else {
            PropagationValue collectingValue = new PropagationValue();
            Iterator var3 = authorities.iterator();

            while(var3.hasNext()) {
                String authority = (String)var3.next();
                PathValue branchValue = new PathValue();
                ScalarFieldValue schemeFieldValue = new ScalarFieldValue("content");
                branchValue.addFieldEntry("scheme", schemeFieldValue);
                ScalarFieldValue authorityFieldValue = new ScalarFieldValue(authority);
                branchValue.addFieldEntry("authority", authorityFieldValue);
                collectingValue.addPathValue(branchValue);
            }

            return collectingValue;
        }
    }

    private int getIdForUnit(Unit unit, SootMethod method) {
        int id = 0;

        for(Iterator var4 = method.getActiveBody().getUnits().iterator(); var4.hasNext(); ++id) {
            Unit currentUnit = (Unit)var4.next();
            if(currentUnit == unit) {
                return id;
            }
        }

        return -1;
    }

    private void analyzeResult(Result result) {
        HashSet nonLinkingFieldNames = new HashSet();
        nonLinkingFieldNames.add("extras");
        nonLinkingFieldNames.add("flags");
        nonLinkingFieldNames.add("fragment");
        nonLinkingFieldNames.add("query");
        Iterator var3 = result.getResults().entrySet().iterator();

        while(var3.hasNext()) {
            Entry entry0 = (Entry)var3.next();
            Collection argumentValues = ((Map)entry0.getValue()).values();
            boolean top = false;
            boolean bottom = false;
            boolean preciseLinking = true;
            boolean preciseNonLinking = true;
            boolean nonexistent = false;
            boolean intentWithUri = false;
            boolean entryPointIntent = false;
            int resultIndex = this.getResultIndex((Stmt)entry0.getKey());
            Iterator var14 = argumentValues.iterator();

            while(true) {
                while(var14.hasNext()) {
                    Object value2 = var14.next();
                    if(value2 == null) {
                        nonexistent = true;
                    } else if(value2 instanceof TopPropagationValue) {
                        top = true;
                    } else if(value2 instanceof BottomPropagationValue) {
                        bottom = true;
                    } else if(value2 instanceof PropagationValue) {
                        Set pathValues = ((PropagationValue)value2).getPathValues();
                        PropagationTimers var10000 = PropagationTimers.v();
                        var10000.pathValues += pathValues.size();
                        HashSet definedFields = new HashSet();
                        Iterator separateFieldValues = pathValues.iterator();

                        while(separateFieldValues.hasNext()) {
                            PathValue separateCount = (PathValue)separateFieldValues.next();
                            definedFields.addAll(separateCount.getFieldMap().keySet());
                        }

                        HashMap var28 = new HashMap();
                        Iterator var29 = pathValues.iterator();

                        while(var29.hasNext()) {
                            PathValue branchValue = (PathValue)var29.next();
                            intentWithUri = intentWithUri || this.isIntentWithUri(branchValue.getFieldMap());
                            HashSet values = new HashSet();
                            Iterator undefinedFieldForPath = branchValue.getFieldMap().entrySet().iterator();

                            while(true) {
                                String fieldName;
                                while(undefinedFieldForPath.hasNext()) {
                                    Entry entry = (Entry)undefinedFieldForPath.next();
                                    fieldName = (String)entry.getKey();
                                    FieldValue fieldValue = (FieldValue)entry.getValue();
                                    this.addValueToSetMap(fieldName, fieldValue, var28);
                                    values.add(fieldName);
                                    if(fieldValue instanceof TopFieldValue) {
                                        if(nonLinkingFieldNames.contains(fieldName)) {
                                            preciseNonLinking = false;
                                        } else {
                                            preciseNonLinking = false;
                                            preciseLinking = false;
                                        }
                                    } else {
                                        Object value = fieldValue.getValue();
                                        if(value != null) {
                                            if(value instanceof Set) {
                                                Set values1 = (Set)value;
                                                if(values1.contains("(.*)") || values1.contains("(.*)") || values1.contains(Integer.valueOf(-1)) || values1.contains("<INTENT>") || values1.contains("top")) {
                                                    if(values1.contains("<INTENT>")) {
                                                        entryPointIntent = true;
                                                    }

                                                    preciseNonLinking = false;
                                                    if(!nonLinkingFieldNames.contains(fieldName)) {
                                                        preciseLinking = false;
                                                    }
                                                }
                                            } else if(value.equals("(.*)") || value.equals("(.*)") || value.equals(Integer.valueOf(-1)) || value.equals("<INTENT>") || value.equals("top")) {
                                                if(value.equals("<INTENT>")) {
                                                    entryPointIntent = true;
                                                }

                                                preciseNonLinking = false;
                                                if(!nonLinkingFieldNames.contains(fieldName)) {
                                                    preciseLinking = false;
                                                }
                                            }
                                        }
                                    }
                                }

                                HashSet var33 = new HashSet(definedFields);
                                var33.removeAll(values);
                                Iterator var34 = var33.iterator();

                                while(var34.hasNext()) {
                                    fieldName = (String)var34.next();
                                    this.addValueToSetMap(fieldName, (FieldValue)null, var28);
                                }
                                break;
                            }
                        }

                        int var30 = 1;

                        Set var32;
                        for(Iterator var31 = var28.values().iterator(); var31.hasNext(); var30 *= var32.size()) {
                            var32 = (Set)var31.next();
                        }

                        var10000 = PropagationTimers.v();
                        var10000.separatePathValues += var30;
                    }
                }

                if(intentWithUri) {
                    ++this.intentWithData;
                }

                if(nonexistent) {
                    if(Scene.v().getActiveHierarchy().isClassSubclassOfIncluding(AnalysisParameters.v().getIcfg().getMethodOf((Unit)entry0.getKey()).getDeclaringClass(), Scene.v().getSootClass("android.content.ContentProvider"))) {
                        ++this.providerArgument;
                    } else {
                        ++this.nonexistent[resultIndex];
                    }
                } else if(top) {
                    ++this.top[resultIndex];
                } else if(bottom) {
                    ++this.bottom[resultIndex];
                } else if(preciseNonLinking) {
                    if(intentWithUri) {
                        ++this.preciseNonLinking[3];
                    } else {
                        ++this.preciseNonLinking[resultIndex];
                    }
                } else if(preciseLinking) {
                    if(intentWithUri) {
                        ++this.preciseLinking[3];
                    } else {
                        ++this.preciseLinking[resultIndex];
                    }
                } else if(entryPointIntent) {
                    ++this.imprecise[4];
                } else if(intentWithUri) {
                    ++this.imprecise[3];
                } else {
                    ++this.imprecise[resultIndex];
                }
                break;
            }
        }

    }

    private void addValueToSetMap(String key, FieldValue value, Map<String, Set<FieldValue>> map) {
        Set<FieldValue> separateValuesForField = (Set)map.get(key);
        if(separateValuesForField == null) {
            separateValuesForField = new HashSet();
            map.put(key, separateValuesForField);
        }

        ((Set)separateValuesForField).add(value);
    }

    private boolean isIntentWithUri(Map<String, FieldValue> fieldMap) {
        Set fields = fieldMap.keySet();
        return (fields.contains("action") || fields.contains("categories")) && (fields.contains("uri") && fieldMap.get("uri") != null && ((FieldValue)fieldMap.get("uri")).getValue() != null || fields.contains("path") && fieldMap.get("path") != null && ((FieldValue)fieldMap.get("path")).getValue() != null || fields.contains("scheme") && fieldMap.get("scheme") != null && ((FieldValue)fieldMap.get("scheme")).getValue() != null || fields.contains("ssp") && fieldMap.get("ssp") != null && ((FieldValue)fieldMap.get("ssp")).getValue() != null);
    }

    private int getResultIndex(Stmt stmt) {
        InvokeExpr invokeExpr = stmt.getInvokeExpr();
        List types = invokeExpr.getMethod().getParameterTypes();
        Iterator var4 = types.iterator();

        Type type;
        do {
            if(!var4.hasNext()) {
                return 0;
            }

            type = (Type)var4.next();
            if(type.toString().equals("android.content.IntentFilter")) {
                return 1;
            }
        } while(!type.toString().equals("android.net.Uri"));

        return 2;
    }

    private boolean containsPartialDefinition(Set<Object> values) {
        Iterator var2 = values.iterator();

        Object value;
        do {
            if(!var2.hasNext()) {
                return false;
            }

            value = var2.next();
        } while(!(value instanceof String) || !((String)value).contains("(.*)"));

        return true;
    }
}
