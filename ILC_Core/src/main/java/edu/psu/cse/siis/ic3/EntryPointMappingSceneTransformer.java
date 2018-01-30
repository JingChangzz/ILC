//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package edu.psu.cse.siis.ic3;

import edu.psu.cse.siis.coal.AnalysisParameters;
import edu.psu.cse.siis.coal.PropagationTimers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import soot.Hierarchy;
import soot.MethodOrMethodContext;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.jimple.infoflow.entryPointCreators.AndroidEntryPointConstants;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.jimple.toolkits.callgraph.Filter;
import soot.jimple.toolkits.callgraph.ReachableMethods;
import soot.util.queue.QueueReader;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class EntryPointMappingSceneTransformer extends SceneTransformer {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private static SootClass activityClass = null;
    private static SootClass serviceClass = null;
    private static SootClass gcmBaseIntentServiceClass = null;
    private static SootClass receiverClass = null;
    private static SootClass providerClass = null;
    private static SootClass applicationClass = null;
    private static SootClass plainClass = null;
    private final Set<String> entryPointClasses;
    private final Map<String, Set<String>> callbackMethods;
    private final Map<SootMethod, Set<String>> entryPointMap;
    private final Set<SootMethod> visitedEntryPoints = new HashSet();

    public EntryPointMappingSceneTransformer(Set<String> entryPointClasses, Map<String, Set<String>> callbackMethods, Map<SootMethod, Set<String>> entryPointMap) {
        this.entryPointClasses = entryPointClasses;
        this.callbackMethods = callbackMethods;
        this.entryPointMap = entryPointMap;
    }

    protected void internalTransform(String phaseName, Map options) {
        PropagationTimers.v().totalTimer.start();
        Timers.v().entryPointMapping.start();
        Map entryPointMap = this.entryPointMap;
        if(this.logger.isDebugEnabled()) {
            HashSet cg = new HashSet(this.callbackMethods.keySet());
            cg.removeAll(this.entryPointClasses);
            if(cg.size() == 0) {
                this.logger.debug("Difference size is 0");
            } else {
                this.logger.debug("Difference is " + cg);
            }
        }

        activityClass = Scene.v().getSootClass("android.app.Activity");
        serviceClass = Scene.v().getSootClass("android.app.Service");
        gcmBaseIntentServiceClass = Scene.v().getSootClass("com.google.android.gcm.GCMBaseIntentService");
        receiverClass = Scene.v().getSootClass("android.content.BroadcastReceiver");
        providerClass = Scene.v().getSootClass("android.content.ContentProvider");
        applicationClass = Scene.v().getSootClass("android.app.Application");
        if(this.logger.isDebugEnabled()) {
            this.logger.debug(this.callbackMethods.toString());
        }

        Iterator cg1 = this.entryPointClasses.iterator();

        label117:
        while(cg1.hasNext()) {
            String it = (String)cg1.next();
            SootClass stringBuilder = Scene.v().getSootClass(it);
            ArrayList e = new ArrayList();
            boolean knownComponentType = this.addLifecycleMethods(stringBuilder, e);
            Iterator callbackMethodStrings = stringBuilder.getMethods().iterator();

            while(true) {
                SootMethod reachableMethods;
                String iter;
                do {
                    if(!callbackMethodStrings.hasNext()) {
                        Set callbackMethodStrings1 = (Set)this.callbackMethods.get(it);
                        SootMethod method;
                        if(callbackMethodStrings1 != null) {
                            Iterator reachableMethods1 = callbackMethodStrings1.iterator();

                            label109:
                            while(true) {
                                while(true) {
                                    if(!reachableMethods1.hasNext()) {
                                        break label109;
                                    }

                                    iter = (String)reachableMethods1.next();
                                    if(!Scene.v().containsMethod(iter)) {
                                        if(this.logger.isWarnEnabled()) {
                                            this.logger.warn("Warning: " + iter + " is not in scene");
                                        }
                                    } else {
                                        method = Scene.v().getMethod(iter);
                                        Iterator entryPoints = method.getDeclaringClass().getMethods().iterator();

                                        while(entryPoints.hasNext()) {
                                            SootMethod potentialInit = (SootMethod)entryPoints.next();
                                            if(!potentialInit.isPrivate()) {
                                                String name = potentialInit.getName();
                                                if(name.equals("<init>")) {
                                                    this.addConstructorStack(potentialInit, e);
                                                } else if(name.equals("<clinit>")) {
                                                    e.add(potentialInit);
                                                }
                                            }
                                        }

                                        e.add(method);
                                    }
                                }
                            }
                        }

                        if(this.logger.isDebugEnabled()) {
                            this.logger.debug(e.toString());
                        }

                        ReachableMethods reachableMethods2 = new ReachableMethods(Scene.v().getCallGraph(), e.iterator(), (Filter)null);
                        reachableMethods2.update();
                        QueueReader iter1 = reachableMethods2.listener();

                        while(iter1.hasNext()) {
                            method = ((MethodOrMethodContext)iter1.next()).method();
                            if(AnalysisParameters.v().isAnalysisClass(method.getDeclaringClass().getName())) {
                                if(this.logger.isDebugEnabled()) {
                                    this.logger.debug(method.toString());
                                }

                                Object entryPoints1 = (Set)entryPointMap.get(method);
                                if(entryPoints1 == null) {
                                    entryPoints1 = new HashSet();
                                    entryPointMap.put(method, entryPoints1);
                                }

                                ((Set)entryPoints1).add(it);
                            }
                        }
                        continue label117;
                    }

                    reachableMethods = (SootMethod)callbackMethodStrings.next();
                    iter = reachableMethods.getName();
                } while(!iter.equals("<init>") && !iter.equals("<clinit>") && knownComponentType);

                e.add(reachableMethods);
            }
        }

        if(this.logger.isDebugEnabled()) {
            this.logger.debug("Entry points");
            this.logger.debug(entryPointMap.toString());
            CallGraph cg2 = Scene.v().getCallGraph();
            QueueReader it1 = cg2.listener();
            StringBuilder stringBuilder1 = new StringBuilder("Call graph:\n");

            while(it1.hasNext()) {
                Edge e1 = (Edge)it1.next();
                stringBuilder1.append("" + e1.src() + e1.srcStmt() + " =" + e1.kind() + "=> " + e1.tgt() + "\n");
            }

            this.logger.debug(stringBuilder1.toString());
        }

        Timers.v().entryPointMapping.end();
        PropagationTimers.v().totalTimer.end();
    }

    private boolean addLifecycleMethods(SootClass entryPointClass, List<MethodOrMethodContext> callbacks) {
        boolean result = true;
        Hierarchy hierarchy = Scene.v().getActiveHierarchy();
        if(hierarchy.isClassSubclassOf(entryPointClass, activityClass)) {
            this.addLifecycleMethodsHelper(entryPointClass, AndroidEntryPointConstants.getActivityLifecycleMethods(), callbacks);
        } else if(hierarchy.isClassSubclassOf(entryPointClass, gcmBaseIntentServiceClass)) {
            this.addLifecycleMethodsHelper(entryPointClass, AndroidEntryPointConstants.getGCMIntentServiceMethods(), callbacks);
        } else if(hierarchy.isClassSubclassOf(entryPointClass, serviceClass)) {
            this.addLifecycleMethodsHelper(entryPointClass, AndroidEntryPointConstants.getServiceLifecycleMethods(), callbacks);
        } else if(hierarchy.isClassSubclassOf(entryPointClass, receiverClass)) {
            this.addLifecycleMethodsHelper(entryPointClass, AndroidEntryPointConstants.getBroadcastLifecycleMethods(), callbacks);
        } else if(hierarchy.isClassSubclassOf(entryPointClass, providerClass)) {
            this.addLifecycleMethodsHelper(entryPointClass, AndroidEntryPointConstants.getContentproviderLifecycleMethods(), callbacks);
        } else if(hierarchy.isClassSubclassOf(entryPointClass, applicationClass)) {
            this.addLifecycleMethodsHelper(entryPointClass, AndroidEntryPointConstants.getApplicationLifecycleMethods(), callbacks);
        } else {
            System.err.println("Unknown entry point type: " + entryPointClass);
            result = false;
        }

        return result;
    }

    private void addLifecycleMethodsHelper(SootClass entryPointClass, List<String> lifecycleMethods, List<MethodOrMethodContext> callbacks) {
        Iterator var4 = lifecycleMethods.iterator();

        while(var4.hasNext()) {
            String lifecycleMethod = (String)var4.next();
            SootMethod method = this.findMethod(entryPointClass, lifecycleMethod);
            if(method != null) {
                callbacks.add(method);
            }
        }

    }

    protected SootMethod findMethod(SootClass currentClass, String subsignature) {
        return currentClass.declaresMethod(subsignature)?currentClass.getMethod(subsignature):(currentClass.hasSuperclass()?this.findMethod(currentClass.getSuperclass(), subsignature):null);
    }

    private void addConstructorStack(SootMethod method, List<MethodOrMethodContext> callbacks) {
        if(!this.visitedEntryPoints.contains(method)) {
            callbacks.add(method);
            this.visitedEntryPoints.add(method);
            Iterator var3 = method.getParameterTypes().iterator();

            while(true) {
                while(true) {
                    String typeString;
                    do {
                        if(!var3.hasNext()) {
                            return;
                        }

                        Type type = (Type)var3.next();
                        typeString = type.toString();
                    } while(!AnalysisParameters.v().isAnalysisClass(typeString));

                    if(Scene.v().containsClass(typeString)) {
                        SootClass sootClass = Scene.v().getSootClass(typeString);
                        Iterator var7 = sootClass.getMethods().iterator();

                        while(var7.hasNext()) {
                            SootMethod sootMethod = (SootMethod)var7.next();
                            if(sootMethod.getName().equals("<init>")) {
                                this.addConstructorStack(sootMethod, callbacks);
                            }
                        }
                    } else if(this.logger.isWarnEnabled()) {
                        this.logger.warn("Warning: " + typeString + " is not in scene");
                    }
                }
            }
        }
    }
}
