//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package soot.jimple.infoflow.android.source;

import heros.InterproceduralCFG;
import soot.PrimType;
import soot.RefType;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.DefinitionStmt;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.infoflow.android.resources.LayoutControl;
import soot.jimple.infoflow.data.AccessPath;
import soot.jimple.infoflow.data.AccessPathFactory;
import soot.jimple.infoflow.data.SootMethodAndClass;
import soot.jimple.infoflow.source.SourceInfo;
import soot.jimple.infoflow.source.data.AccessPathTuple;
import soot.jimple.infoflow.source.data.SourceSinkDefinition;
import soot.jimple.infoflow.util.SystemClassHandler;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class AccessPathBasedSourceSinkManager extends AndroidSourceSinkManager {
    public AccessPathBasedSourceSinkManager(Set<SourceSinkDefinition> sources, Set<SourceSinkDefinition> sinks) {
        super(sources, sinks);
    }

    public AccessPathBasedSourceSinkManager(Set<SourceSinkDefinition> sources, Set<SourceSinkDefinition> sinks, Set<SootMethodAndClass> callbackMethods, LayoutMatchingMode layoutMatching, Map<Integer, LayoutControl> layoutControls) {
        super(sources, sinks, callbackMethods, layoutMatching, layoutControls);
    }

    public SourceInfo getSourceInfo(Stmt sCallSite, InterproceduralCFG<Unit, SootMethod> cfg) {
        SourceType type = this.getSourceType(sCallSite, cfg);
        if(type == SourceType.NoSource) {
            return null;
        } else if(type != SourceType.Callback && type != SourceType.UISource) {
            String signature = (String)this.methodToSignature.getUnchecked(sCallSite.getInvokeExpr().getMethod());
            SourceSinkDefinition def = (SourceSinkDefinition)this.sourceMethods.get(signature);
            if(null != def && !def.isEmpty()) {
                HashSet aps = new HashSet();
                Value i;
                Iterator var8;
                AccessPathTuple apt;
                if(sCallSite.containsInvokeExpr() && sCallSite.getInvokeExpr() instanceof InstanceInvokeExpr && def.getBaseObjects() != null) {
                    i = ((InstanceInvokeExpr)sCallSite.getInvokeExpr()).getBase();
                    var8 = def.getBaseObjects().iterator();

                    while(var8.hasNext()) {
                        apt = (AccessPathTuple)var8.next();
                        if(apt.isSource()) {
                            aps.add(this.getAccessPathFromDef(i, apt));
                        }
                    }
                }

                if(sCallSite instanceof DefinitionStmt && def.getReturnValues() != null) {
                    i = ((DefinitionStmt)sCallSite).getLeftOp();
                    var8 = def.getReturnValues().iterator();

                    while(var8.hasNext()) {
                        apt = (AccessPathTuple)var8.next();
                        if(apt.isSource()) {
                            aps.add(this.getAccessPathFromDef(i, apt));
                        }
                    }
                }

                if(sCallSite.containsInvokeExpr() && def.getParameters() != null && def.getParameters().length > 0) {
                    for(int var10 = 0; var10 < sCallSite.getInvokeExpr().getArgCount(); ++var10) {
                        if(def.getParameters().length > var10) {
                            var8 = def.getParameters()[var10].iterator();

                            while(var8.hasNext()) {
                                apt = (AccessPathTuple)var8.next();
                                if(apt.isSource()) {
                                    aps.add(this.getAccessPathFromDef(sCallSite.getInvokeExpr().getArg(var10), apt));
                                }
                            }
                        }
                    }
                }

                return aps.isEmpty()?super.getSourceInfo(sCallSite, cfg):new SourceInfo(aps);
            } else {
                return super.getSourceInfo(sCallSite, cfg);
            }
        } else {
            return super.getSourceInfo(sCallSite, type);
        }
    }

    private AccessPath getAccessPathFromDef(Value baseVal, AccessPathTuple apt) {
        if(!(baseVal.getType() instanceof PrimType) && apt.getFields() != null && apt.getFields().length != 0) {
            SootClass baseClass = ((RefType)baseVal.getType()).getSootClass();
            SootField[] fields = new SootField[apt.getFields().length];

            for(int i = 0; i < fields.length; ++i) {
                fields[i] = baseClass.getFieldByName(apt.getFields()[i]);
            }

            return AccessPathFactory.v().createAccessPath(baseVal, fields, true);
        } else {
            return AccessPathFactory.v().createAccessPath(baseVal, true);
        }
    }

    public boolean isSink(Stmt sCallSite, InterproceduralCFG<Unit, SootMethod> cfg, AccessPath sourceAccessPath) {
        if(!sCallSite.containsInvokeExpr()) {
            return false;
        } else {
            String methodSignature = (String)this.methodToSignature.getUnchecked(sCallSite.getInvokeExpr().getMethod());
            SourceSinkDefinition def = (SourceSinkDefinition)this.sinkMethods.get(methodSignature);
            Iterator i;
            if(def == null) {
                i = cfg.getCalleesOfCallAt(sCallSite).iterator();

                String var12;
                do {
                    if(!i.hasNext()) {
                        return false;
                    }

                    SootMethod var11 = (SootMethod)i.next();
                    var12 = (String)this.methodToSignature.getUnchecked(var11);
                } while(!this.sinkMethods.containsKey(var12));

                return true;
            } else if(def.isEmpty()) {
                return SystemClassHandler.isTaintVisible(sourceAccessPath, sCallSite.getInvokeExpr().getMethod());
            } else if(sourceAccessPath == null) {
                return true;
            } else {
                if(sCallSite.getInvokeExpr() instanceof InstanceInvokeExpr && def.getBaseObjects() != null) {
                    i = def.getBaseObjects().iterator();

                    while(i.hasNext()) {
                        AccessPathTuple apt = (AccessPathTuple)i.next();
                        if(apt.isSink() && this.accessPathMatches(sourceAccessPath, apt)) {
                            return true;
                        }
                    }
                }

                if(def.getParameters() != null && def.getParameters().length > 0) {
                    for(int var9 = 0; var9 < sCallSite.getInvokeExpr().getArgCount(); ++var9) {
                        if(sCallSite.getInvokeExpr().getArg(var9) == sourceAccessPath.getPlainValue() && def.getParameters().length > var9) {
                            Iterator var10 = def.getParameters()[var9].iterator();

                            while(var10.hasNext()) {
                                AccessPathTuple apt1 = (AccessPathTuple)var10.next();
                                if(apt1.isSink() && this.accessPathMatches(sourceAccessPath, apt1)) {
                                    return true;
                                }
                            }
                        }
                    }
                }

                return false;
            }
        }
    }

    private boolean accessPathMatches(AccessPath sourceAccessPath, AccessPathTuple apt) {
        if(apt.getFields() != null && apt.getFields().length != 0 && sourceAccessPath != null) {
            for(int i = 0; i < apt.getFields().length; ++i) {
                if(i >= sourceAccessPath.getFieldCount()) {
                    return sourceAccessPath.getTaintSubFields();
                }

                if(!sourceAccessPath.getFields()[i].getName().equals(apt.getFields()[i])) {
                    return false;
                }
            }

            return true;
        } else {
            return true;
        }
    }
}
