//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package soot.jimple.infoflow.android;

import ilc.main.Core;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParserException;
import soot.G;
import soot.Main;
import soot.PackManager;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.jimple.Stmt;
import soot.jimple.infoflow.AbstractInfoflow;
import soot.jimple.infoflow.Infoflow;
import soot.jimple.infoflow.android.TestApps.Test;
import soot.jimple.infoflow.android.callbacks.AbstractCallbackAnalyzer;
import soot.jimple.infoflow.android.callbacks.DefaultCallbackAnalyzer;
import soot.jimple.infoflow.android.config.SootConfigForAndroid;
import soot.jimple.infoflow.android.data.AndroidMethod;
import soot.jimple.infoflow.android.data.parsers.PermissionMethodParser;
import soot.jimple.infoflow.android.resources.ARSCFileParser;
import soot.jimple.infoflow.android.resources.LayoutControl;
import soot.jimple.infoflow.android.resources.LayoutFileParser;
import soot.jimple.infoflow.android.source.AccessPathBasedSourceSinkManager;
import soot.jimple.infoflow.android.source.parsers.xml.XMLSourceSinkParser;
import soot.jimple.infoflow.cfg.BiDirICFGFactory;
import soot.jimple.infoflow.config.IInfoflowConfig;
import soot.jimple.infoflow.data.SootMethodAndClass;
import soot.jimple.infoflow.data.pathBuilders.DefaultPathBuilderFactory;
import soot.jimple.infoflow.entryPointCreators.AndroidEntryPointCreator;
import soot.jimple.infoflow.handlers.ResultsAvailableHandler;
import soot.jimple.infoflow.ipc.IIPCManager;
import soot.jimple.infoflow.results.InfoflowResults;
import soot.jimple.infoflow.rifl.RIFLSourceSinkDefinitionProvider;
import soot.jimple.infoflow.source.data.ISourceSinkDefinitionProvider;
import soot.jimple.infoflow.source.data.SourceSinkDefinition;
import soot.jimple.infoflow.taintWrappers.ITaintPropagationWrapper;
import soot.options.Options;

import javax.activation.UnsupportedDataTypeException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SetupApplication {
    private final Logger logger;
    private ISourceSinkDefinitionProvider sourceSinkProvider;
    private final Map<String, Set<SootMethodAndClass>> callbackMethods;
    private InfoflowAndroidConfiguration config;
    private Set<String> entrypoints;
    private Set<String> callbackClasses;
    private String appPackageName;
    private final String androidJar;
    private final boolean forceAndroidJar;
    private final String apkFileLocation;
    private final String additionalClasspath;
    private ITaintPropagationWrapper taintWrapper;
    private AccessPathBasedSourceSinkManager sourceSinkManager;
    private AndroidEntryPointCreator entryPointCreator;
    private IInfoflowConfig sootConfig;
    private BiDirICFGFactory cfgFactory;
    private IIPCManager ipcManager;
    private long maxMemoryConsumption;
    private Set<Stmt> collectedSources;
    private Set<Stmt> collectedSinks;
    private String callbackFile;
    private List<ARSCFileParser.ResPackage> resourcePackages;

    public SetupApplication(String androidJar, String apkFileLocation) {
        this(androidJar, apkFileLocation, "", (IIPCManager)null);
    }

    public SetupApplication(String androidJar, String apkFileLocation, IIPCManager ipcManager) {
        this(androidJar, apkFileLocation, "", ipcManager);
    }

    public SetupApplication(String androidJar, String apkFileLocation, String additionalClasspath, IIPCManager ipcManager) {
        this.logger = LoggerFactory.getLogger(this.getClass());
        this.callbackMethods = new HashMap(10000);
        this.config = new InfoflowAndroidConfiguration();
        this.entrypoints = null;
        this.callbackClasses = null;
        this.appPackageName = "";
        this.sourceSinkManager = null;
        this.entryPointCreator = null;
        this.sootConfig = new SootConfigForAndroid();
        this.cfgFactory = null;
        this.ipcManager = null;
        this.maxMemoryConsumption = -1L;
        this.collectedSources = null;
        this.collectedSinks = null;
        this.callbackFile = "AndroidCallbacks.txt";
        File f = new File(androidJar);
        this.forceAndroidJar = f.isFile();
        this.androidJar = androidJar;
        this.apkFileLocation = apkFileLocation;
        this.ipcManager = ipcManager;
        this.additionalClasspath = additionalClasspath;
        this.resourcePackages = null;
    }

    public Set<SourceSinkDefinition> getSinks() {
        return this.sourceSinkProvider == null?null:this.sourceSinkProvider.getSinks();
    }

    public Set<Stmt> getCollectedSinks() {
        return this.collectedSinks;
    }

    public void printSinks() {
        if(this.sourceSinkProvider == null) {
            System.err.println("Sinks not calculated yet");
        } else {
            System.out.println("Sinks:");
            Iterator var1 = this.getSinks().iterator();

            while(var1.hasNext()) {
                SourceSinkDefinition am = (SourceSinkDefinition)var1.next();
                System.out.println(am.toString());
            }

            System.out.println("End of Sinks");
        }
    }

    public Set<SourceSinkDefinition> getSources() {
        return this.sourceSinkProvider == null?null:this.sourceSinkProvider.getSources();
    }

    public Set<Stmt> getCollectedSources() {
        return this.collectedSources;
    }

    public void printSources() {
        if(this.sourceSinkProvider == null) {
            System.err.println("Sources not calculated yet");
        } else {
            System.out.println("Sources:");
            Iterator var1 = this.getSources().iterator();

            while(var1.hasNext()) {
                SourceSinkDefinition am = (SourceSinkDefinition)var1.next();
                System.out.println(am.toString());
            }

            System.out.println("End of Sources");
        }
    }

    public Set<String> getEntrypointClasses() {
        return this.entrypoints;
    }

    public void printEntrypoints() {
        if(this.entrypoints == null) {
            System.out.println("Entry points not initialized");
        } else {
            System.out.println("Classes containing entry points:");
            Iterator var1 = this.entrypoints.iterator();

            while(var1.hasNext()) {
                String className = (String)var1.next();
                System.out.println("\t" + className);
            }

            System.out.println("End of Entrypoints");
        }

    }

    public void setCallbackClasses(Set<String> callbackClasses) {
        this.callbackClasses = callbackClasses;
    }

    public Set<String> getCallbackClasses() {
        return this.callbackClasses;
    }

    public void setTaintWrapper(ITaintPropagationWrapper taintWrapper) {
        this.taintWrapper = taintWrapper;
    }

    public ITaintPropagationWrapper getTaintWrapper() {
        return this.taintWrapper;
    }

    public void calculateSourcesSinksEntrypoints(Set<AndroidMethod> sources, Set<AndroidMethod> sinks) throws IOException, XmlPullParserException {
        final HashSet sourceDefs = new HashSet(sources.size());
        final HashSet sinkDefs = new HashSet(sinks.size());
        Iterator parser = sources.iterator();

        AndroidMethod am;
        while(parser.hasNext()) {
            am = (AndroidMethod)parser.next();
            sourceDefs.add(new SourceSinkDefinition(am));
        }

        parser = sinks.iterator();

        while(parser.hasNext()) {
            am = (AndroidMethod)parser.next();
            sinkDefs.add(new SourceSinkDefinition(am));
        }

        ISourceSinkDefinitionProvider parser1 = new ISourceSinkDefinitionProvider() {
            public Set<SourceSinkDefinition> getSources() {
                return sourceDefs;
            }

            public Set<SourceSinkDefinition> getSinks() {
                return sinkDefs;
            }

            public Set<SourceSinkDefinition> getAllMethods() {
                HashSet sourcesSinks = new HashSet(sourceDefs.size() + sinkDefs.size());
                sourcesSinks.addAll(sourceDefs);
                sourcesSinks.addAll(sinkDefs);
                return sourcesSinks;
            }
        };
        this.calculateSourcesSinksEntrypoints(parser1);
    }

    public void calculateSourcesSinksEntrypoints(String sourceSinkFile) throws IOException, XmlPullParserException {
        Object parser = null;
        String fileExtension = sourceSinkFile.substring(sourceSinkFile.lastIndexOf("."));
        fileExtension = fileExtension.toLowerCase();

        try {
            if(fileExtension.equals(".xml")) {
                parser = XMLSourceSinkParser.fromFile(sourceSinkFile);
            } else if(fileExtension.equals(".txt")) {
                parser = PermissionMethodParser.fromFile(sourceSinkFile);
            } else {
                if(!fileExtension.equals(".rifl")) {
                    throw new UnsupportedDataTypeException("The Inputfile isn\'t a .txt or .xml file.");
                }

                parser = new RIFLSourceSinkDefinitionProvider(sourceSinkFile);
            }

            this.calculateSourcesSinksEntrypoints((ISourceSinkDefinitionProvider)parser);
        } catch (SAXException var5) {
            throw new IOException("Could not read XML file", var5);
        }
    }

    public void calculateSourcesSinksEntrypoints(ISourceSinkDefinitionProvider sourcesAndSinks) throws IOException, XmlPullParserException {
        this.sourceSinkProvider = sourcesAndSinks;
        this.entrypoints = Test.entryPoint;
        String apkName = new File(this.apkFileLocation).getName();
        this.appPackageName = apkName.substring(0, apkName.lastIndexOf("."));
        if (Core.resDir!=null && Core.arscFile!=null){
            ARSCFileParser resParser = new ARSCFileParser();
            resParser.parse(Core.arscFile);
            this.resourcePackages = resParser.getPackages();
            LayoutFileParser lfp = null;
            if(this.config.getEnableCallbacks()) {
                if (this.callbackClasses != null && this.callbackClasses.isEmpty()) {
                    this.logger.warn("Callback definition file is empty, disabling callbacks");
                } else {
                    lfp = new LayoutFileParser((String)this.entrypoints.toArray()[0], resParser);
                    this.calculateCallbackMethods(resParser, lfp);
                }
            }
        }
        System.out.println("Entry point calculation done.");
        G.reset();
        //no callback
        HashSet callbacks = new HashSet();
        Iterator var8 = this.callbackMethods.values().iterator();

        while(var8.hasNext()) {
            Set methods = (Set)var8.next();
            callbacks.addAll(methods);
        }

        this.sourceSinkManager = new AccessPathBasedSourceSinkManager(this.sourceSinkProvider.getSources(), this.sourceSinkProvider.getSinks(), callbacks, this.config.getLayoutMatchingMode(), null);
        this.sourceSinkManager.setAppPackageName(this.appPackageName);
        this.sourceSinkManager.setResourcePackages(null);
        this.sourceSinkManager.setEnableCallbackSources(this.config.getEnableCallbackSources());
        //
        this.entryPointCreator = this.createEntryPointCreator();
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
                jimpleClass = this.callbackClasses == null?new DefaultCallbackAnalyzer(this.config, this.entrypoints, this.callbackFile):new DefaultCallbackAnalyzer(this.config, this.entrypoints, this.callbackClasses);
                jimpleClass.collectCallbackMethods();
                lfp.parseLayoutFile(Core.resDir);
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
        System.out.println("277:createMainMethod" + entryPoint.toString());
        Scene.v().setEntryPoints(Collections.singletonList(entryPoint));
        if(Scene.v().containsClass(entryPoint.getDeclaringClass().getName())) {
            Scene.v().removeClass(entryPoint.getDeclaringClass());
        }

        Scene.v().addClass(entryPoint.getDeclaringClass());
        entryPoint.getDeclaringClass().setApplicationClass();
    }

    public AccessPathBasedSourceSinkManager getSourceSinkManager() {
        return this.sourceSinkManager;
    }

    private String getClasspath() {
        String classpath = this.forceAndroidJar?this.androidJar:Scene.v().getAndroidJarPath(this.androidJar, this.apkFileLocation);
        if(this.additionalClasspath != null && !this.additionalClasspath.isEmpty()) {
            classpath = classpath + File.pathSeparator + this.additionalClasspath;
        }

        this.logger.debug("soot classpath: " + classpath);
        return classpath;
    }

    private void initializeSoot(boolean constructCallgraph) {
        Options.v().set_no_bodies_for_excluded(true);
        Options.v().set_allow_phantom_refs(true);
        Options.v().set_output_format(12);
        Options.v().set_whole_program(constructCallgraph);
        Options.v().set_process_dir(Collections.singletonList(this.apkFileLocation));
        if(this.forceAndroidJar) {
            Options.v().set_force_android_jar(this.androidJar);
        } else {
            Options.v().set_android_jars(this.androidJar);
        }

        Options.v().set_src_prec(6);
        Options.v().set_keep_line_number(false);
        Options.v().set_keep_offset(false);
        if(this.sootConfig != null) {
            this.sootConfig.setSootOptions(Options.v());
        }

        Options.v().set_soot_classpath(this.getClasspath());
        Main.v().autoSetOptions();
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

    public InfoflowResults runInfoflow() {
        return this.runInfoflow((ResultsAvailableHandler)null);
    }

    public InfoflowResults runInfoflow(ResultsAvailableHandler onResultsAvailable) {
        if(this.sourceSinkProvider == null) {
            throw new RuntimeException("Sources and/or sinks not calculated yet");
        } else {
            System.out.println("Running data flow analysis on " + this.apkFileLocation + " with " + this.getSources().size() + " sources and " + this.getSinks().size() + " sinks...");
            Infoflow info;
            if(this.cfgFactory == null) {
                info = new Infoflow(this.androidJar, this.forceAndroidJar, (BiDirICFGFactory)null, new DefaultPathBuilderFactory(this.config.getPathBuilder(), this.config.getComputeResultPaths()));
            } else {
                info = new Infoflow(this.androidJar, this.forceAndroidJar, this.cfgFactory, new DefaultPathBuilderFactory(this.config.getPathBuilder(), this.config.getComputeResultPaths()));
            }

            String path;
            if(this.forceAndroidJar) {
                path = this.androidJar;
            } else {
                path = Scene.v().getAndroidJarPath(this.androidJar, this.apkFileLocation);
            }

            info.setTaintWrapper(this.taintWrapper);
            if(onResultsAvailable != null) {
                info.addResultsAvailableHandler(onResultsAvailable);
            }

            System.out.println("Starting infoflow computation...");
            info.setConfig(this.config);
            info.setSootConfig(this.sootConfig);
            if(null != this.ipcManager) {
                info.setIPCManager(this.ipcManager);
            }

            if(!info.computeInfoflow(this.apkFileLocation, path, this.entryPointCreator, this.sourceSinkManager)) {
                Test.InfoFlowComputationTimeOut = true;
            }

            this.maxMemoryConsumption = info.getMaxMemoryConsumption();
            this.collectedSources = info.getCollectedSources();
            this.collectedSinks = info.getCollectedSinks();
            return info.getResults();
        }
    }

    private AndroidEntryPointCreator createEntryPointCreator() {
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

    public AndroidEntryPointCreator getEntryPointCreator() {
        return this.entryPointCreator;
    }

    public IInfoflowConfig getSootConfig() {
        return this.sootConfig;
    }

    public void setSootConfig(IInfoflowConfig config) {
        this.sootConfig = config;
    }

    public void setIcfgFactory(BiDirICFGFactory factory) {
        this.cfgFactory = factory;
    }

    public long getMaxMemoryConsumption() {
        return this.maxMemoryConsumption;
    }

    public InfoflowAndroidConfiguration getConfig() {
        return this.config;
    }

    public void setConfig(InfoflowAndroidConfiguration config) {
        this.config = config;
    }

    public void setCallbackFile(String callbackFile) {
        this.callbackFile = callbackFile;
    }

    public void calculateSourcesSinksEntrypoints(ArrayList<String> data) throws IOException, XmlPullParserException {
        PermissionMethodParser parser = null;
        parser = PermissionMethodParser.fromStringList(data);
        this.calculateSourcesSinksEntrypoints((ISourceSinkDefinitionProvider)parser);
    }
}
