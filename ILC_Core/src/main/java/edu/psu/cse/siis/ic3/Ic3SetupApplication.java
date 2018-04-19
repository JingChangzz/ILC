//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package edu.psu.cse.siis.ic3;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import soot.G;
import soot.Main;
import soot.PackManager;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.jimple.infoflow.AbstractInfoflow;
import soot.jimple.infoflow.SequentialEntryPointCreator;
import soot.jimple.infoflow.android.InfoflowAndroidConfiguration;
import soot.jimple.infoflow.android.callbacks.AbstractCallbackAnalyzer;
import soot.jimple.infoflow.android.callbacks.DefaultCallbackAnalyzer;
import soot.jimple.infoflow.android.config.SootConfigForAndroid;
import soot.jimple.infoflow.android.data.AndroidMethod;
import soot.jimple.infoflow.android.resources.ARSCFileParser;
import soot.jimple.infoflow.android.resources.LayoutControl;
import soot.jimple.infoflow.android.resources.LayoutFileParser;
import soot.jimple.infoflow.config.IInfoflowConfig;
import soot.jimple.infoflow.data.SootMethodAndClass;
import soot.jimple.infoflow.entryPointCreators.AndroidEntryPointCreator;
import soot.options.Options;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class Ic3SetupApplication {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final Map<String, Set<SootMethodAndClass>> callbackMethods = new HashMap(10000);
    private Set<String> entrypoints = null;
    private final Set<String> callbackClasses = null;
    private String appPackageName = "";
    private final String callbackFile = "AndroidCallbacks.txt";
    private final String apkFileLocation;
    private final String classDirectory;
    private final String androidClassPath;
    private final InfoflowAndroidConfiguration config = new InfoflowAndroidConfiguration();
    private final IInfoflowConfig sootConfig = new SootConfigForAndroid();
    private AndroidEntryPointCreator entryPointCreator;
    private SequentialEntryPointCreator sequentialEntryPointCreator;

    public Ic3SetupApplication(String apkFileLocation, String classDirectory, String androidClassPath) {
        this.apkFileLocation = apkFileLocation;
        this.classDirectory = classDirectory;
        this.androidClassPath = androidClassPath;
    }

    public AndroidEntryPointCreator getEntryPointCreator() {
        return this.entryPointCreator;
    }


    public SequentialEntryPointCreator getSequentialEntryPointCreator() {
        sequentialEntryPointCreator = new SequentialEntryPointCreator(new ArrayList<>(this.entrypoints));
        return sequentialEntryPointCreator;
    }

    public void printEntrypoints() {
        if(this.logger.isDebugEnabled()) {
            if(this.entrypoints == null) {
                this.logger.debug("Entry points not initialized");
            } else {
                this.logger.debug("Classes containing entry points:");
                Iterator var1 = this.entrypoints.iterator();

                while(var1.hasNext()) {
                    String className = (String)var1.next();
                    this.logger.debug("\t" + className);
                }

                this.logger.debug("End of Entrypoints");
            }
        }

    }

    private void calculateCallbackMethods(ARSCFileParser resParser, LayoutFileParser lfp) throws IOException {
        DefaultCallbackAnalyzer jimpleClass = null;
        boolean hasChanged = true;

        while(hasChanged) {
            hasChanged = false;
            G.reset();
            this.initializeSoot(true);
            this.createMainMethod();
            if(jimpleClass == null) {
                jimpleClass = this.callbackClasses == null?new DefaultCallbackAnalyzer(this.config, this.entrypoints, "AndroidCallbacks.txt"):new DefaultCallbackAnalyzer(this.config, this.entrypoints, this.callbackClasses);
                jimpleClass.collectCallbackMethods();
                lfp.parseLayoutFile(Ic3Main.resDir);
            } else {
                jimpleClass.collectCallbackMethodsIncremental();
            }

            PackManager.v().getPack("wjpp").apply();
            PackManager.v().getPack("cg").apply();
            PackManager.v().getPack("wjtp").apply();
            Iterator var5 = jimpleClass.getCallbackMethods().entrySet().iterator();

            while(var5.hasNext()) {
                Map.Entry entry = (Map.Entry)var5.next();
                Set curCallbacks = (Set)this.callbackMethods.get(entry.getKey());
                if(curCallbacks != null) {
                    if(curCallbacks.addAll((Collection)entry.getValue())) {
                        hasChanged = true;
                    }
                } else {
                    this.callbackMethods.put((String) entry.getKey(), new HashSet((Collection)entry.getValue()));
                    hasChanged = true;
                }
            }

            if(this.entrypoints.addAll(jimpleClass.getDynamicManifestComponents())) {
                hasChanged = true;
            }
        }

        this.collectXmlBasedCallbackMethods(resParser, lfp, jimpleClass);
    }

    private void collectXmlBasedCallbackMethods(ARSCFileParser resParser, LayoutFileParser lfp, AbstractCallbackAnalyzer jimpleClass) {
        Iterator callbacksPlain = jimpleClass.getLayoutClasses().entrySet().iterator();

        label75:
        while(callbacksPlain.hasNext()) {
            Map.Entry lcentry = (Map.Entry)callbacksPlain.next();
            SootClass set = Scene.v().getSootClass((String)lcentry.getKey());
            Iterator var7 = ((Set)lcentry.getValue()).iterator();

            while(true) {
                Set controls1;
                do {
                    while(true) {
                        if(!var7.hasNext()) {
                            continue label75;
                        }

                        Integer classId = (Integer)var7.next();
                        ARSCFileParser.AbstractResource resource = resParser.findResource(classId.intValue());
                        if(resource instanceof ARSCFileParser.StringResource) {
                            String layoutFileName = ((ARSCFileParser.StringResource)resource).getValue();
                            Set callbackMethods = (Set)lfp.getCallbackMethods().get(layoutFileName);
                            if(callbackMethods != null) {
                                Iterator controls = callbackMethods.iterator();

                                label61:
                                while(true) {
                                    while(true) {
                                        if(!controls.hasNext()) {
                                            break label61;
                                        }

                                        String methodName = (String)controls.next();
                                        String lc = "void " + methodName + "(android.view.View)";
                                        SootClass currentClass = set;

                                        while(true) {
                                            SootMethod callbackMethod = currentClass.getMethodUnsafe(lc);
                                            if(callbackMethod != null) {
                                                this.addCallbackMethod(set.getName(), new AndroidMethod(callbackMethod));
                                                break;
                                            }

                                            if(!currentClass.hasSuperclass()) {
                                                System.err.println("Callback method " + methodName + " not found in class " + set.getName());
                                                break;
                                            }

                                            currentClass = currentClass.getSuperclass();
                                        }
                                    }
                                }
                            }

                            controls1 = (Set)lfp.getUserControls().get(layoutFileName);
                            break;
                        }

                        System.err.println("Unexpected resource type for layout class");
                    }
                } while(controls1 == null);

                Iterator methodName1 = controls1.iterator();

                while(methodName1.hasNext()) {
                    LayoutControl lc1 = (LayoutControl)methodName1.next();
                    this.registerCallbackMethodsForView(set, lc1);
                }
            }
        }

        HashSet callbacksPlain1 = new HashSet();
        Iterator lcentry1 = this.callbackMethods.values().iterator();

        while(lcentry1.hasNext()) {
            Set set1 = (Set)lcentry1.next();
            callbacksPlain1.addAll(set1);
        }

        System.out.println("Found " + callbacksPlain1.size() + " callback methods for " + this.callbackMethods.size() + " components");
    }

    public Map<String, Set<String>> calculateSourcesSinksEntrypoints(Set<AndroidMethod> sourceMethods, Set<AndroidMethod> modifierMethods, String packageName, Set<String> entryPointClasses) throws IOException {
        this.appPackageName = packageName;
        this.entrypoints = entryPointClasses;
        boolean parseLayoutFile = !this.apkFileLocation.endsWith(".xml");
        ARSCFileParser resParser = null;
        if(Ic3Main.arscFile!=null) {   //相当于只有aar才进行这个操作
            resParser = new ARSCFileParser();
            resParser.parse(Ic3Main.arscFile);
            Object jimpleClass = null;
            LayoutFileParser lfp = parseLayoutFile ? new LayoutFileParser(this.appPackageName, resParser) : null;
            this.calculateCallbackMethods(resParser, lfp);
        }
        this.logger.info("Entry point calculation done.");
        G.reset();
        HashMap result = new HashMap(this.callbackMethods.size());
        Iterator var10 = this.callbackMethods.entrySet().iterator();

        while(var10.hasNext()) {
            Map.Entry entry = (Map.Entry)var10.next();
            Set callbackSet = (Set)entry.getValue();
            HashSet callbackStrings = new HashSet(callbackSet.size());
            Iterator var14 = callbackSet.iterator();

            while(var14.hasNext()) {
                SootMethodAndClass androidMethod = (SootMethodAndClass)var14.next();
                callbackStrings.add(androidMethod.getSignature());
            }

            result.put(entry.getKey(), callbackStrings);
        }

        this.entryPointCreator = this.createEntryPointCreator();
        return result;
    }

    private void registerCallbackMethodsForView(SootClass callbackClass, LayoutControl lc) {
        if(!callbackClass.getName().startsWith("android.")) {
            if(!lc.getViewClass().getName().startsWith("android.")) {
                SootClass sc = lc.getViewClass();

                boolean systemMethods;
                for(systemMethods = false; sc.hasSuperclass(); sc = sc.getSuperclass()) {
                    if(sc.getName().equals("android.view.View")) {
                        systemMethods = true;
                        break;
                    }
                }

                if(systemMethods) {
                    sc = lc.getViewClass();
                    HashSet systemMethods1 = new HashSet(10000);
                    Iterator var5 = Scene.v().getActiveHierarchy().getSuperclassesOf(sc).iterator();

                    while(true) {
                        SootClass sm;
                        do {
                            if(!var5.hasNext()) {
                                var5 = sc.getMethods().iterator();

                                while(var5.hasNext()) {
                                    SootMethod sm2 = (SootMethod)var5.next();
                                    if(!sm2.isConstructor() && systemMethods1.contains(sm2.getSubSignature())) {
                                        this.addCallbackMethod(callbackClass.getName(), new AndroidMethod(sm2));
                                    }
                                }

                                return;
                            }

                            sm = (SootClass)var5.next();
                        } while(!sm.getName().startsWith("android."));

                        Iterator var7 = sm.getMethods().iterator();

                        while(var7.hasNext()) {
                            SootMethod sm1 = (SootMethod)var7.next();
                            if(!sm1.isConstructor()) {
                                systemMethods1.add(sm1.getSubSignature());
                            }
                        }
                    }
                }
            }
        }
    }

    private void addCallbackMethod(String layoutClass, AndroidMethod callbackMethod) {
        Set<SootMethodAndClass> methods = (Set)this.callbackMethods.get(layoutClass);
        if(methods == null) {
            methods = new HashSet();
            this.callbackMethods.put(layoutClass, methods);
        }

        ((Set)methods).add(new AndroidMethod(callbackMethod));
    }

    private void createMainMethod() {
        SootMethod entryPoint = this.createEntryPointCreator().createDummyMain();
        Scene.v().setEntryPoints(Collections.singletonList(entryPoint));
        if(Scene.v().containsClass(entryPoint.getDeclaringClass().getName())) {
            Scene.v().removeClass(entryPoint.getDeclaringClass());
        }

        Scene.v().addClass(entryPoint.getDeclaringClass());
    }

    public void initializeSoot() {
        G.reset();
        Options.v().set_ignore_resolution_errors(true);
        Options.v().set_debug(false);
        Options.v().set_verbose(false);
        Options.v().set_unfriendly_mode(true);
        Options.v().set_no_bodies_for_excluded(true);
        Options.v().set_allow_phantom_refs(true);
        Options.v().set_output_format(12);
        Options.v().set_whole_program(true);
        Options.v().setPhaseOption("cg.spark", "on");
        Options.v().set_ignore_resolution_errors(true);
        Options.v().set_soot_classpath(this.apkFileLocation + File.pathSeparator + this.androidClassPath);
        if(this.logger.isDebugEnabled()) {
            this.logger.debug("Android class path: " + this.androidClassPath);
        }

        Options.v().set_force_android_jar("ic3-android.jar");
        Options.v().set_src_prec(5);
        Options.v().set_process_dir(new ArrayList(this.entrypoints));
        Main.v().autoSetOptions();
        Scene.v().loadNecessaryClasses();
    }

    private String getClasspath() {
        boolean forceAndroidJar = true;
        String classpath = forceAndroidJar?this.androidClassPath:Scene.v().getAndroidJarPath(this.androidClassPath, this.apkFileLocation);
        this.logger.debug("soot classpath: " + classpath);
        return classpath;
    }

    private void initializeSoot(boolean constructCallgraph) {
        Options.v().set_no_bodies_for_excluded(true);
        Options.v().set_allow_phantom_refs(true);
        Options.v().set_output_format(12);
        Options.v().set_whole_program(constructCallgraph);
        Options.v().set_process_dir(Collections.singletonList(this.apkFileLocation));
        Options.v().set_soot_classpath(this.getClasspath());
        Options.v().set_src_prec(6);
        Options.v().set_keep_line_number(false);
        Options.v().set_keep_offset(false);
        Main.v().autoSetOptions();
        if(this.sootConfig != null) {
            this.sootConfig.setSootOptions(Options.v());
        }

        if(constructCallgraph) {
            switch(this.config.getCallgraphAlgorithm()) {
                case AutomaticSelection:
                case SPARK:
                    Options.v().setPhaseOption("cg.spark", "on");
                    break;
                case GEOM:
                    Options.v().setPhaseOption("cg.spark", "on");
                    AbstractInfoflow.setGeomPtaSpecificOptions();
                    break;
                case CHA:
                    Options.v().setPhaseOption("cg.cha", "on");
                    break;
                case RTA:
                    Options.v().setPhaseOption("cg.spark", "on");
                    Options.v().setPhaseOption("cg.spark", "rta:true");
                    Options.v().setPhaseOption("cg.spark", "on-fly-cg:false");
                    break;
                case VTA:
                    Options.v().setPhaseOption("cg.spark", "on");
                    Options.v().setPhaseOption("cg.spark", "vta:true");
                    break;
                default:
                    throw new RuntimeException("Invalid callgraph algorithm");
            }
        }

        Scene.v().loadNecessaryClasses();
    }

    public AndroidEntryPointCreator createEntryPointCreator() {
        AndroidEntryPointCreator entryPointCreator = new AndroidEntryPointCreator(new ArrayList(this.entrypoints));
        HashMap callbackMethodSigs = new HashMap();
        Iterator var3 = this.callbackMethods.keySet().iterator();

        while(var3.hasNext()) {
            String className = (String)var3.next();
            ArrayList methodSigs = new ArrayList();
            callbackMethodSigs.put(className, methodSigs);
            Iterator var6 = ((Set)this.callbackMethods.get(className)).iterator();

            while(var6.hasNext()) {
                SootMethodAndClass am = (SootMethodAndClass)var6.next();
                methodSigs.add(am.getSignature());
            }
        }

        entryPointCreator.setCallbackFunctions(callbackMethodSigs);
        return entryPointCreator;
    }
}
