//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package edu.psu.cse.siis.ic3;

import edu.psu.cse.siis.coal.AnalysisParameters;
import edu.psu.cse.siis.coal.Model;
import edu.psu.cse.siis.coal.PropagationSolver;
import edu.psu.cse.siis.coal.PropagationTimers;
import edu.psu.cse.siis.coal.Result;
import edu.psu.cse.siis.coal.ResultBuilder;
import edu.psu.cse.siis.coal.arguments.Argument;
import edu.psu.cse.siis.coal.arguments.ArgumentValueManager;
import edu.psu.cse.siis.coal.values.BasePropagationValue;
import edu.psu.cse.siis.coal.values.PropagationValue;
import soot.MethodOrMethodContext;
import soot.Scene;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.toolkits.callgraph.Filter;
import soot.jimple.toolkits.callgraph.ReachableMethods;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.util.queue.QueueReader;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

public class Ic3ResultBuilder implements ResultBuilder {
    private Map<SootMethod, Set<String>> entryPointMap;

    public Ic3ResultBuilder() {
    }

    public void setEntryPointMap(Map<SootMethod, Set<String>> entryPointMap) {
        this.entryPointMap = entryPointMap;
    }

    public Result buildResult(PropagationSolver solver) {
        PropagationTimers.v().resultGeneration.start();
        Ic3Result result = new Ic3Result(this.entryPointMap);
        ArrayList eps = new ArrayList(Scene.v().getEntryPoints());
        ReachableMethods reachableMethods = new ReachableMethods(Scene.v().getCallGraph(), eps.iterator(), (Filter)null);
        reachableMethods.update();
        long reachableStatements = 0L;

        PropagationTimers var10000;
        for(QueueReader iter = reachableMethods.listener(); iter.hasNext(); var10000.reachableStatements += reachableStatements) {
            SootMethod method = ((MethodOrMethodContext)iter.next()).method();
            if(method.hasActiveBody() && !Model.v().isExcludedClass(method.getDeclaringClass().getName()) && !method.getDeclaringClass().getName().equals("dummyMainClass") && !method.getDeclaringClass().getName().startsWith("android.support") && !method.getDeclaringClass().getName().startsWith("android.provider")) {
                ++PropagationTimers.v().reachableMethods;
                ExceptionalUnitGraph cfg = new ExceptionalUnitGraph(method.getActiveBody());
                Stack stack = new Stack();
                Iterator visited = cfg.getHeads().iterator();

                Unit unit;
                while(visited.hasNext()) {
                    unit = (Unit)visited.next();
                    stack.push(unit);
                }

                HashSet var24 = new HashSet();

                label94:
                while(true) {
                    Argument[] var25;
                    do {
                        do {
                            if(stack.empty()) {
                                reachableStatements += (long)var24.size();
                                break label94;
                            }

                            unit = (Unit)stack.pop();
                        } while(var24.contains(unit));

                        var24.add(unit);
                        Iterator arguments = cfg.getSuccsOf(unit).iterator();

                        while(arguments.hasNext()) {
                            Unit foundModeledType = (Unit)arguments.next();
                            stack.push(foundModeledType);
                        }

                        var25 = Model.v().getArgumentsForQuery((Stmt)unit);
                    } while(var25 == null);

                    boolean var26 = false;
                    Argument[] stmt = var25;
                    int var16 = var25.length;

                    int var17;
                    for(var17 = 0; var17 < var16; ++var17) {
                        Argument argument = stmt[var17];
                        if(Model.v().isModeledType(argument.getType())) {
                            var26 = true;
                            break;
                        }
                    }

                    Stmt var27 = (Stmt)unit;
                    Argument[] var28 = var25;
                    var17 = var25.length;

                    for(int var29 = 0; var29 < var17; ++var29) {
                        Argument argument1 = var28[var29];
                        if(Model.v().isModeledType(argument1.getType())) {
                            int argnum = argument1.getArgnum()[0];
                            InvokeExpr invokeExpr = var27.getInvokeExpr();
                            BasePropagationValue basePropagationValue;
                            if(argnum >= 0) {
                                basePropagationValue = (BasePropagationValue)solver.resultAt(unit, invokeExpr.getArg(argnum));
                            } else {
                                if(!(invokeExpr instanceof InstanceInvokeExpr) || argnum != -1) {
                                    throw new RuntimeException("Unexpected argument number " + argnum + " for invoke expression " + invokeExpr);
                                }

                                InstanceInvokeExpr propagationValue = (InstanceInvokeExpr)invokeExpr;
                                basePropagationValue = (BasePropagationValue)solver.resultAt(var27, propagationValue.getBase());
                            }

                            if(basePropagationValue instanceof PropagationValue) {
                                PropagationValue var30 = (PropagationValue)basePropagationValue;
                                PropagationTimers.v().resultGeneration.end();
                                PropagationTimers.v().valueComposition.start();
                                var30.makeFinalValue(solver);
                                PropagationTimers.v().valueComposition.end();
                                PropagationTimers.v().resultGeneration.start();
                                result.addResult(unit, argument1.getArgnum()[0], var30);
                            } else {
                                result.addResult(unit, argument1.getArgnum()[0], basePropagationValue);
                            }
                        } else if(var26 || AnalysisParameters.v().inferNonModeledTypes()) {
                            result.addResult(unit, argument1.getArgnum()[0], ArgumentValueManager.v().getArgumentValues(argument1, unit));
                        }
                    }
                }
            }

            var10000 = PropagationTimers.v();
        }

        PropagationTimers.v().resultGeneration.end();
        return result;
    }
}
