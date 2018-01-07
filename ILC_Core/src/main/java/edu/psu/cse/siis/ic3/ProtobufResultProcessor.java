//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package edu.psu.cse.siis.ic3;

import com.google.protobuf.TextFormat;
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
import edu.psu.cse.siis.ic3.Ic3Data.Application.Builder;
import edu.psu.cse.siis.ic3.Ic3Data.Application.Component.ComponentKind;
import edu.psu.cse.siis.ic3.Ic3Data.Application.Component.ExitPoint;
import edu.psu.cse.siis.ic3.Ic3Data.Application.Component.ExitPoint.Intent;
import edu.psu.cse.siis.ic3.Ic3Data.Application.Component.ExitPoint.Uri;
import edu.psu.cse.siis.ic3.Ic3Data.Application.Component.Instruction;
import edu.psu.cse.siis.ic3.Ic3Data.Attribute;
import edu.psu.cse.siis.ic3.Ic3Data.AttributeKind;
import edu.psu.cse.siis.ic3.manifest.ManifestComponent;
import edu.psu.cse.siis.ic3.manifest.ManifestData;
import edu.psu.cse.siis.ic3.manifest.ManifestIntentFilter;
import edu.psu.cse.siis.ic3.manifest.ManifestPullParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import soot.Scene;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
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

public class ProtobufResultProcessor {
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

    public ProtobufResultProcessor() {
    }

    public void processResult(String appName, Builder ic3Builder, String protobufDestination, boolean binary, Map<String, edu.psu.cse.siis.ic3.Ic3Data.Application.Component.Builder> componentNameToBuilderMap, int analysisClassesCount, Writer writer) throws IOException {
        Iterator extension = Results.getResults().iterator();

        while(extension.hasNext()) {
            Result path = (Result)extension.next();
            ((Ic3Result)path).dump();
            this.analyzeResult(path);
            this.writeResultToProtobuf(path, ic3Builder, componentNameToBuilderMap);
        }

        ic3Builder.setAnalysisEnd(System.currentTimeMillis() / 1000L);
        String extension1 = binary?"dat":"txt";
        String path1 = String.format("%s/%s_%s.%s", new Object[]{protobufDestination, ic3Builder.getName(), Integer.valueOf(ic3Builder.getVersion()), extension1});
        System.out.println("PATH: " + path1);
        if(binary) {
            FileOutputStream statistics = new FileOutputStream(path1);
            ic3Builder.build().writeTo(statistics);
            statistics.close();
        } else {
            FileWriter statistics1 = new FileWriter(path1);
            TextFormat.print(ic3Builder, statistics1);
            statistics1.close();
        }

        Timers.v().totalTimer.end();
        String statistics2 = appName + " " + analysisClassesCount + " " + PropagationTimers.v().reachableMethods + " " + this.preciseNonLinking[0] + " " + this.preciseNonLinking[3] + " " + this.preciseNonLinking[1] + " " + this.preciseNonLinking[2] + " " + this.preciseLinking[0] + " " + this.preciseLinking[3] + " " + this.preciseLinking[1] + " " + this.preciseLinking[2] + " " + this.imprecise[0] + " " + this.imprecise[3] + " " + this.imprecise[1] + " " + this.imprecise[2] + " " + this.bottom[0] + " " + this.bottom[1] + " " + this.bottom[2] + " " + this.top[0] + " " + this.top[1] + " " + this.top[2] + " " + this.nonexistent[0] + " " + this.nonexistent[1] + " " + this.nonexistent[2] + " " + this.providerArgument + " " + this.imprecise[4] + " " + this.preciseFieldValueCount[0] + " " + this.preciseFieldValueCount[1] + " " + this.preciseFieldValueCount[2] + " " + this.partiallyPreciseFieldValueCount[0] + " " + this.partiallyPreciseFieldValueCount[1] + " " + this.partiallyPreciseFieldValueCount[2] + " " + this.impreciseFieldValueCount[0] + " " + this.impreciseFieldValueCount[1] + " " + this.impreciseFieldValueCount[2] + " " + PropagationTimers.v().modelParsing.getTime() + " " + Timers.v().mainGeneration.getTime() + " " + Timers.v().entryPointMapping.getTime() + " " + Timers.v().classLoading.getTime() + " " + PropagationTimers.v().problemGeneration.getTime() + " " + PropagationTimers.v().ideSolution.getTime() + " " + PropagationTimers.v().valueComposition.getTime() + " " + PropagationTimers.v().resultGeneration.getTime() + " " + (PropagationTimers.v().soot.getTime() - PropagationTimers.v().totalTimer.getTime()) + " " + (Timers.v().misc.getTime() + PropagationTimers.v().misc.getTime()) + " " + Timers.v().totalTimer.getTime() + "\n";
        if(this.logger.isInfoEnabled()) {
            this.logger.info(statistics2);
        }

        if(writer != null) {
            writer.write(statistics2);
            writer.close();
        }

    }

    private void writeResultToProtobuf(Result result, Builder ic3Builder, Map<String, edu.psu.cse.siis.ic3.Ic3Data.Application.Component.Builder> componentNameToBuilderMap) {
        HashMap componentToExtrasMap = new HashMap();
        HashMap dynamicReceivers = new HashMap();
        Map entryPointMap = ((Ic3Result)result).getEntryPointMap();
        Iterator var7 = result.getResults().entrySet().iterator();

        while(true) {
            while(true) {
                Entry manifestComponent;
                Unit componentBuilder;
                Argument[] arguments;
                do {
                    if(!var7.hasNext()) {
                        var7 = componentToExtrasMap.entrySet().iterator();

                        while(var7.hasNext()) {
                            manifestComponent = (Entry)var7.next();
                            ((edu.psu.cse.siis.ic3.Ic3Data.Application.Component.Builder)componentNameToBuilderMap.get(manifestComponent.getKey())).addAllExtras((Iterable)manifestComponent.getValue());
                        }

                        var7 = componentNameToBuilderMap.values().iterator();

                        while(var7.hasNext()) {
                            edu.psu.cse.siis.ic3.Ic3Data.Application.Component.Builder var20 = (edu.psu.cse.siis.ic3.Ic3Data.Application.Component.Builder)var7.next();
                            ic3Builder.addComponents(var20);
                        }

                        var7 = dynamicReceivers.values().iterator();

                        while(var7.hasNext()) {
                            ManifestComponent var21 = (ManifestComponent)var7.next();
                            edu.psu.cse.siis.ic3.Ic3Data.Application.Component.Builder var22 = ManifestPullParser.makeProtobufComponentBuilder(var21, ComponentKind.DYNAMIC_RECEIVER);
                            var22.setRegistrationInstruction(this.unitToInstructionBuilder(var21.getRegistrationMethod(), var21.getRegistrationUnit()));
                            ic3Builder.addComponents(var22);
                        }

                        return;
                    }

                    manifestComponent = (Entry)var7.next();
                    componentBuilder = (Unit)manifestComponent.getKey();
                    arguments = Model.v().getArgumentsForQuery((Stmt)componentBuilder);
                } while(arguments == null);

                SootMethod method = AnalysisParameters.v().getIcfg().getMethodOf(componentBuilder);
                edu.psu.cse.siis.ic3.Ic3Data.Application.Component.Instruction.Builder instructionBuilder = this.unitToInstructionBuilder(method, componentBuilder);
                HashMap valueMap = new HashMap(arguments.length);
                Map argnumToValueMap = (Map)manifestComponent.getValue();
                Argument[] extras = arguments;
                int targetType = arguments.length;

                for(int permissions = 0; permissions < targetType; ++permissions) {
                    Argument argument = extras[permissions];
                    valueMap.put(argument.getProperty("valueType"), argnumToValueMap.get(Integer.valueOf(argument.getArgnum()[0])));
                }

                if(valueMap.containsKey("activity")) {
                    this.insertProtobufExitPoint(instructionBuilder, (BasePropagationValue)valueMap.get("activity"), ComponentKind.ACTIVITY, (Set)null, (Integer)null, (Set)entryPointMap.get(method), componentNameToBuilderMap);
                } else if(valueMap.containsKey("service")) {
                    this.insertProtobufExitPoint(instructionBuilder, (BasePropagationValue)valueMap.get("service"), ComponentKind.SERVICE, (Set)null, (Integer)null, (Set)entryPointMap.get(method), componentNameToBuilderMap);
                } else if(valueMap.containsKey("receiver")) {
                    this.insertProtobufExitPoint(instructionBuilder, (BasePropagationValue)valueMap.get("receiver"), ComponentKind.RECEIVER, (Set)valueMap.get("permission"), (Integer)null, (Set)entryPointMap.get(method), componentNameToBuilderMap);
                } else if(valueMap.containsKey("intentFilter")) {
                    this.insertDynamicReceiver(dynamicReceivers, (Set)valueMap.get("permission"), (Set)valueMap.get("receiverType"), (BasePropagationValue)valueMap.get("intentFilter"), method, componentBuilder);
                } else if(valueMap.containsKey("provider")) {
                    this.insertProtobufExitPoint(instructionBuilder, (BasePropagationValue)valueMap.get("provider"), ComponentKind.PROVIDER, (Set)null, (Integer)null, (Set)entryPointMap.get(method), componentNameToBuilderMap);
                } else if(valueMap.containsKey("authority")) {
                    this.insertProtobufExitPoint(instructionBuilder, this.getUriValueForAuthorities((Set)valueMap.get("authority")), ComponentKind.PROVIDER, (Set)null, (Integer)null, (Set)entryPointMap.get(method), componentNameToBuilderMap);
                } else if(valueMap.containsKey("pendingIntent")) {
                    BasePropagationValue var24 = (BasePropagationValue)valueMap.get("pendingIntent");
                    String var25 = var24 instanceof PropagationValue?(String)((FieldValue)((PropagationValue)var24).getValuesForField("targetType").iterator().next()).getValue():null;
                    Set var26 = (Set)valueMap.get("permission");
                    if(var25 != null) {
                        this.insertProtobufExitPoint(instructionBuilder, var24, this.stringToComponentKind(var25), var26, (Integer)null, (Set)entryPointMap.get(method), componentNameToBuilderMap);
                    } else {
                        Iterator var27 = Arrays.asList(new ComponentKind[]{ComponentKind.ACTIVITY, ComponentKind.RECEIVER, ComponentKind.SERVICE}).iterator();

                        while(var27.hasNext()) {
                            ComponentKind target = (ComponentKind)var27.next();
                            this.insertProtobufExitPoint(instructionBuilder, var24, target, (Set)null, (Integer)null, (Set)entryPointMap.get(method), componentNameToBuilderMap);
                        }
                    }
                } else if(valueMap.containsKey("componentExtra")) {
                    Set var23 = (Set)valueMap.get("componentExtra");
                    if(var23 != null) {
                        ;
                    }
                }
            }
        }
    }

    private edu.psu.cse.siis.ic3.Ic3Data.Application.Component.Instruction.Builder unitToInstructionBuilder(SootMethod method, Unit unit) {
        edu.psu.cse.siis.ic3.Ic3Data.Application.Component.Instruction.Builder builder = Instruction.newBuilder();
        builder.setClassName(method.getDeclaringClass().getName());
        builder.setMethod(method.getSignature());
        builder.setStatement(unit.toString());
        builder.setId(this.getIdForUnit(unit, method));
        return builder;
    }

    private void insertProtobufExitPoint(edu.psu.cse.siis.ic3.Ic3Data.Application.Component.Instruction.Builder instructionBuilder, BasePropagationValue intentValue, ComponentKind componentKind, Set<String> intentPermissions, Integer missingIntents, Set<String> exitPointComponents, Map<String, edu.psu.cse.siis.ic3.Ic3Data.Application.Component.Builder> componentNameToBuilderMap) {
        Iterator var8 = exitPointComponents.iterator();

        while(var8.hasNext()) {
            String exitPointComponent = (String)var8.next();
            edu.psu.cse.siis.ic3.Ic3Data.Application.Component.ExitPoint.Builder exitPointBuilder = ExitPoint.newBuilder();
            exitPointBuilder.setInstruction(instructionBuilder).setKind(componentKind);
            PropagationValue collectingValue = null;
            if(intentValue != null && !(intentValue instanceof TopPropagationValue) && !(intentValue instanceof BottomPropagationValue)) {
                if(!(intentValue instanceof PropagationValue)) {
                    throw new RuntimeException("Unknown CollectingValue type: " + intentValue.getClass());
                }

                collectingValue = (PropagationValue)intentValue;
                if(collectingValue.getPathValues() == null || collectingValue.getPathValues().size() == 0) {
                    missingIntents = Integer.valueOf(0);
                }
            } else {
                missingIntents = Integer.valueOf(0);
            }

            if(missingIntents != null) {
                exitPointBuilder.setMissing(missingIntents.intValue());
            } else {
                Set componentBuilder = collectingValue.getPathValues();
                if(componentBuilder != null) {
                    Iterator var13 = componentBuilder.iterator();

                    label64:
                    while(true) {
                        while(true) {
                            if(!var13.hasNext()) {
                                break label64;
                            }

                            PathValue pathValue = (PathValue)var13.next();
                            if(componentKind.equals(ComponentKind.PROVIDER)) {
                                exitPointBuilder.addUris(this.makeProtobufUriBuilder(pathValue));
                            } else if(intentPermissions != null && intentPermissions.size() != 0) {
                                Iterator var15 = intentPermissions.iterator();

                                while(var15.hasNext()) {
                                    String intentPermission = (String)var15.next();
                                    exitPointBuilder.addIntents(this.makeProtobufIntentBuilder(pathValue).setPermission(intentPermission));
                                }
                            } else {
                                exitPointBuilder.addIntents(this.makeProtobufIntentBuilder(pathValue));
                            }
                        }
                    }
                }
            }

            edu.psu.cse.siis.ic3.Ic3Data.Application.Component.Builder componentBuilder1 = (edu.psu.cse.siis.ic3.Ic3Data.Application.Component.Builder)componentNameToBuilderMap.get(exitPointComponent);
            componentBuilder1.addExitPoints(exitPointBuilder);
        }

    }

    private edu.psu.cse.siis.ic3.Ic3Data.Application.Component.ExitPoint.Intent.Builder makeProtobufIntentBuilder(PathValue intentValue) {
        edu.psu.cse.siis.ic3.Ic3Data.Application.Component.ExitPoint.Intent.Builder intentBuilder = Intent.newBuilder();
        this.insertSingleValuedIntentAttribute(intentValue, "action", AttributeKind.ACTION, intentBuilder);
        Set categories = intentValue.getSetStringFieldValue("categories");
        if(categories != null) {
            if(categories.contains((Object)null)) {
                categories.remove((Object)null);
                categories.add("NULL-CONSTANT");
            }

            intentBuilder.addAttributes(Attribute.newBuilder().setKind(AttributeKind.CATEGORY).addAllValue(categories));
        }

        Set flags = intentValue.getSetFieldValue("flags", Integer.class);
        if(flags != null) {
            intentBuilder.addAttributes(Attribute.newBuilder().setKind(AttributeKind.FLAG).addAllIntValue(flags));
        }

        this.insertSingleValuedIntentAttribute(intentValue, "dataType", AttributeKind.TYPE, intentBuilder);
        Set extras = intentValue.getSetStringFieldValue("extras");
        if(extras != null) {
            if(extras.contains((Object)null)) {
                extras.remove((Object)null);
                extras.add("NULL-CONSTANT");
            }

            intentBuilder.addAttributes(Attribute.newBuilder().setKind(AttributeKind.EXTRA).addAllValue(extras));
        }

        this.insertSingleValuedIntentAttribute(intentValue, "clazz", AttributeKind.CLASS, intentBuilder);
        this.insertSingleValuedIntentAttribute(intentValue, "package", AttributeKind.PACKAGE, intentBuilder);
        this.insertSingleValuedIntentAttribute(intentValue, "scheme", AttributeKind.SCHEME, intentBuilder);
        this.insertSingleValuedIntentAttribute(intentValue, "ssp", AttributeKind.SSP, intentBuilder);
        this.insertSingleValuedIntentAttribute(intentValue, "uri", AttributeKind.URI, intentBuilder);
        this.insertSingleValuedIntentAttribute(intentValue, "path", AttributeKind.PATH, intentBuilder);
        this.insertSingleValuedIntentAttribute(intentValue, "query", AttributeKind.QUERY, intentBuilder);
        this.insertSingleValuedIntentAttribute(intentValue, "authority", AttributeKind.AUTHORITY, intentBuilder);
        return intentBuilder;
    }

    private void insertSingleValuedIntentAttribute(PathValue pathValue, String attribute, AttributeKind kind, edu.psu.cse.siis.ic3.Ic3Data.Application.Component.ExitPoint.Intent.Builder intentBuilder) {
        String attributeValue = pathValue.getScalarStringFieldValue(attribute);
        if(attributeValue != null) {
            intentBuilder.addAttributes(Attribute.newBuilder().setKind(kind).addValue(attributeValue));
        }

    }

    private edu.psu.cse.siis.ic3.Ic3Data.Application.Component.ExitPoint.Uri.Builder makeProtobufUriBuilder(PathValue uriValue) {
        edu.psu.cse.siis.ic3.Ic3Data.Application.Component.ExitPoint.Uri.Builder uriBuilder = Uri.newBuilder();
        this.insertSingleValuedUriAttribute(uriValue, "scheme", AttributeKind.SCHEME, uriBuilder);
        this.insertSingleValuedUriAttribute(uriValue, "ssp", AttributeKind.SSP, uriBuilder);
        this.insertSingleValuedUriAttribute(uriValue, "uri", AttributeKind.URI, uriBuilder);
        this.insertSingleValuedUriAttribute(uriValue, "path", AttributeKind.PATH, uriBuilder);
        this.insertSingleValuedUriAttribute(uriValue, "query", AttributeKind.QUERY, uriBuilder);
        this.insertSingleValuedUriAttribute(uriValue, "authority", AttributeKind.AUTHORITY, uriBuilder);
        return uriBuilder;
    }

    private void insertSingleValuedUriAttribute(PathValue pathValue, String attribute, AttributeKind kind, edu.psu.cse.siis.ic3.Ic3Data.Application.Component.ExitPoint.Uri.Builder uriBuilder) {
        String attributeValue = pathValue.getScalarStringFieldValue(attribute);
        if(attributeValue != null) {
            uriBuilder.addAttributes(Attribute.newBuilder().setKind(kind).addValue(attributeValue));
        }

    }

    private ComponentKind stringToComponentKind(String componentKind) {
        byte var3 = -1;
        switch(componentKind.hashCode()) {
            case 97:
                if(componentKind.equals("a")) {
                    var3 = 0;
                }
                break;
            case 114:
                if(componentKind.equals("r")) {
                    var3 = 2;
                }
                break;
            case 115:
                if(componentKind.equals("s")) {
                    var3 = 1;
                }
        }

        switch(var3) {
            case 0:
                return ComponentKind.ACTIVITY;
            case 1:
                return ComponentKind.SERVICE;
            case 2:
                return ComponentKind.RECEIVER;
            default:
                throw new RuntimeException("Unknown component kind: " + componentKind);
        }
    }

    private void insertDynamicReceiver(Map<String, ManifestComponent> dynamicReceivers, Set<String> permissions, Set<String> receiverTypes, BasePropagationValue intentFilters, SootMethod method, Unit unit) {
        if(permissions == null) {
            permissions = Collections.singleton(null);
        }

        Iterator var7 = receiverTypes.iterator();

        while(var7.hasNext()) {
            String receiverType = (String)var7.next();
            Iterator var9 = permissions.iterator();

            while(var9.hasNext()) {
                String permission = (String)var9.next();
                this.insertDynamicReceiverHelper(dynamicReceivers, permission, receiverType, intentFilters, method, unit);
            }
        }

    }

    private void insertDynamicReceiverHelper(Map<String, ManifestComponent> dynamicReceivers, String permission, String receiverType, BasePropagationValue intentFilters, SootMethod method, Unit unit) {
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
            for(Iterator var10 = manifestComponent.getPathValues().iterator(); var10.hasNext(); manifestIntentFilters.add(new ManifestIntentFilter(branchValue.getSetStringFieldValue("actions"), branchValue.getSetStringFieldValue("categories"), false, this.makeManifestData(branchValue), filterPriority))) {
                branchValue = (PathValue)var10.next();
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

        ManifestComponent manifestComponent1 = (ManifestComponent)dynamicReceivers.get(receiverType);
        if(manifestComponent1 == null) {
            manifestComponent1 = new ManifestComponent("d", receiverType, true, true, permission, (String)null, missingIntentFilters, method, unit);
            dynamicReceivers.put(receiverType, manifestComponent1);
        }

        manifestComponent1.addIntentFilters(manifestIntentFilters);
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
                        PropagationValue collectingValue = (PropagationValue)value2;
                        Iterator var17 = collectingValue.getPathValues().iterator();

                        label143:
                        while(var17.hasNext()) {
                            PathValue branchValue = (PathValue)var17.next();
                            intentWithUri = intentWithUri || this.isIntentWithUri(branchValue.getFieldMap());
                            Iterator var19 = branchValue.getFieldMap().entrySet().iterator();

                            while(true) {
                                while(true) {
                                    if(!var19.hasNext()) {
                                        continue label143;
                                    }

                                    Entry entry = (Entry)var19.next();
                                    String fieldName = (String)entry.getKey();
                                    FieldValue fieldValue = (FieldValue)entry.getValue();
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
                                                Set values = (Set)value;
                                                if(values.contains("(.*)") || values.contains("(.*)") || values.contains(Integer.valueOf(-1)) || values.contains("<INTENT>") || values.contains("top")) {
                                                    if(values.contains("<INTENT>")) {
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
                            }
                        }
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
