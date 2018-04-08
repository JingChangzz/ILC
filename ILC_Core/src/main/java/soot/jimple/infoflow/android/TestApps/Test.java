//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package soot.jimple.infoflow.android.TestApps;

import org.xmlpull.v1.XmlPullParserException;
import soot.SootMethod;
import soot.jimple.Stmt;
import soot.jimple.infoflow.InfoflowConfiguration.CallgraphAlgorithm;
import soot.jimple.infoflow.InfoflowManager;
import soot.jimple.infoflow.android.InfoflowAndroidConfiguration;
import soot.jimple.infoflow.android.InfoflowAndroidConfiguration.CallbackAnalyzer;
import soot.jimple.infoflow.android.SetupApplication;
import soot.jimple.infoflow.android.data.ICCEntryPointSourceSink;
import soot.jimple.infoflow.android.data.ICCExitPointSourceSink;
import soot.jimple.infoflow.android.source.AndroidSourceSinkManager.LayoutMatchingMode;
import soot.jimple.infoflow.config.IInfoflowConfig;
import soot.jimple.infoflow.data.Abstraction;
import soot.jimple.infoflow.data.pathBuilders.DefaultPathBuilderFactory.PathBuilder;
import soot.jimple.infoflow.handlers.ResultsAvailableHandler2;
import soot.jimple.infoflow.ipc.IIPCManager;
import soot.jimple.infoflow.results.InfoflowResults;
import soot.jimple.infoflow.results.ResultSinkInfo;
import soot.jimple.infoflow.results.ResultSourceInfo;
import soot.jimple.infoflow.results.xml.InfoflowResultsSerializer;
import soot.jimple.infoflow.solver.cfg.IInfoflowCFG;
import soot.jimple.infoflow.taintWrappers.EasyTaintWrapper;
import soot.jimple.infoflow.taintWrappers.ITaintPropagationWrapper;
import soot.jimple.infoflow.util.SystemClassHandler;
import soot.options.Options;

import javax.xml.stream.XMLStreamException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Test {
    private static InfoflowAndroidConfiguration config = new InfoflowAndroidConfiguration();
    private static int repeatCount = 1;
    private static int timeout = 900;
    private static int sysTimeout = -1;
    private static boolean aggressiveTaintWrapper = false;
    private static boolean noTaintWrapper = false;
    private static String summaryPath = "";
    private static String resultFilePath = "";
    private static boolean DEBUG = true;
    private static InfoflowResults infoResults = null;
    public static boolean InfoFlowComputationTimeOut;
    public static boolean ExitPointSink = true;
    private static IIPCManager ipcManager = null;
    public static Set<String> entryPoint = new HashSet<>();
    public static String androidManifest = "";
    public static String resDirectory = "";
    public Test() {
    }

    public static void setIPCManager(IIPCManager ipcManager) {
        Test.ipcManager = ipcManager;
    }

    public static IIPCManager getIPCManager() {
        return ipcManager;
    }

    public static InfoflowResults runAnalysisForResults(String[] args, Set<String> entryPoints, String manifest, String resDir) throws IOException, InterruptedException {
        if(args.length < 2) {
            printUsage();
            return null;
        } else {
            entryPoint = entryPoints;
            androidManifest = manifest;
            resDirectory = resDir;
            File outputDir = new File("JimpleOutput");
            int oldRepeatCount;
            File line;
            if(outputDir.isDirectory()) {
                boolean apkFiles = true;
                File[] apkFile = outputDir.listFiles();
                int results = apkFile.length;

                for(oldRepeatCount = 0; oldRepeatCount < results; ++oldRepeatCount) {
                    line = apkFile[oldRepeatCount];
                    apkFiles = apkFiles && line.delete();
                }

                if(!apkFiles) {
                    System.err.println("Cleanup of output directory " + outputDir + " failed!");
                }

                outputDir.delete();
            }

            if(!parseAdditionalOptions(args)) {
                return null;
            } else if(!validateAdditionalOptions()) {
                return null;
            } else if(repeatCount <= 0) {
                return null;
            } else {
                ArrayList var10 = new ArrayList();
                File var11 = new File(args[0]);
                String fullFilePath;
                if(var11.isDirectory()) {
                    String[] var12 = var11.list(new FilenameFilter() {
                        public boolean accept(File dir, String name) {
                            return name.endsWith(".jar");
                        }
                    });
                    String[] var15 = var12;
                    int var17 = var12.length;

                    for(int fileName = 0; fileName < var17; ++fileName) {
                        fullFilePath = var15[fileName];
                        var10.add(fullFilePath);
                    }
                } else {
                    String var13 = var11.getName().substring(var11.getName().lastIndexOf("."));
                    if(var13.equalsIgnoreCase(".txt")) {
                        BufferedReader var16 = new BufferedReader(new FileReader(var11));
                        line = null;

                        String var18;
                        while((var18 = var16.readLine()) != null) {
                            var10.add(var18);
                        }

                        var16.close();
                    } else {
                        if(!var13.equalsIgnoreCase(".jar")) {
                            System.err.println("Invalid input file format: " + var13);
                            return null;
                        }

                        var10.add(args[0]);
                    }
                }

                InfoflowResults var14 = null;
                oldRepeatCount = repeatCount;
                Iterator var19 = var10.iterator();

                while(true) {
                    while(true) {
                        if(!var19.hasNext()) {
                            return var14;
                        }

                        String var20 = (String)var19.next();
                        repeatCount = oldRepeatCount;
                        System.gc();
                        if(var10.size() > 1) {
                            if(var11.isDirectory()) {
                                fullFilePath = args[0] + File.separator + var20;
                            } else {
                                fullFilePath = var20;
                            }

                            System.out.println("Analyzing file " + fullFilePath + "...");
                            File flagFile = new File("_Run_" + (new File(var20)).getName());
                            if(flagFile.exists()) {
                                continue;
                            }

                            flagFile.createNewFile();
                            break;
                        }

                        fullFilePath = var20;
                        break;
                    }

                    var14 = runAnalysisTimeout(fullFilePath, args[1]);
                    System.gc();
                }
            }
        }
    }

    private static boolean parseAdditionalOptions(String[] args) {
        int i = 2;
        ExitPointSink = true;
        //config.setCodeEliminationMode(InfoflowConfiguration.CodeEliminationMode.NoCodeElimination);
        while(i < args.length) {
            if(args[i].equalsIgnoreCase("--timeout")) {
                timeout = Integer.valueOf(args[i + 1]).intValue();
                i += 2;
            } else if(args[i].equalsIgnoreCase("--systimeout")) {
                sysTimeout = Integer.valueOf(args[i + 1]).intValue();
                i += 2;
            } else if(args[i].equalsIgnoreCase("--singleflow")) {
                config.setStopAfterFirstFlow(true);
                ++i;
            } else if(args[i].equalsIgnoreCase("--implicit")) {
                config.setEnableImplicitFlows(true);
                ++i;
            } else if(args[i].equalsIgnoreCase("--nostatic")) {
                config.setEnableStaticFieldTracking(false);
                ++i;
            } else if(args[i].equalsIgnoreCase("--aplength")) {
                InfoflowAndroidConfiguration.setAccessPathLength(Integer.valueOf(args[i + 1]).intValue());
                i += 2;
            } else if(args[i].equalsIgnoreCase("--iccentry")) {
                ExitPointSink = false;
                ++i;
            } else {
                String algo;
                if(args[i].equalsIgnoreCase("--cgalgo")) {
                    algo = args[i + 1];
                    if(algo.equalsIgnoreCase("AUTO")) {
                        config.setCallgraphAlgorithm(CallgraphAlgorithm.AutomaticSelection);
                    } else if(algo.equalsIgnoreCase("CHA")) {
                        config.setCallgraphAlgorithm(CallgraphAlgorithm.CHA);
                    } else if(algo.equalsIgnoreCase("VTA")) {
                        config.setCallgraphAlgorithm(CallgraphAlgorithm.VTA);
                    } else if(algo.equalsIgnoreCase("RTA")) {
                        config.setCallgraphAlgorithm(CallgraphAlgorithm.RTA);
                    } else if(algo.equalsIgnoreCase("SPARK")) {
                        config.setCallgraphAlgorithm(CallgraphAlgorithm.SPARK);
                    } else {
                        if(!algo.equalsIgnoreCase("GEOM")) {
                            System.err.println("Invalid callgraph algorithm");
                            return false;
                        }

                        config.setCallgraphAlgorithm(CallgraphAlgorithm.GEOM);
                    }

                    i += 2;
                } else if(args[i].equalsIgnoreCase("--nocallbacks")) {
                    config.setEnableCallbacks(false);
                    ++i;
                } else if(args[i].equalsIgnoreCase("--noexceptions")) {
                    config.setEnableExceptionTracking(false);
                    ++i;
                } else if(args[i].equalsIgnoreCase("--layoutmode")) {
                    algo = args[i + 1];
                    if(algo.equalsIgnoreCase("NONE")) {
                        config.setLayoutMatchingMode(LayoutMatchingMode.NoMatch);
                    } else if(algo.equalsIgnoreCase("PWD")) {
                        config.setLayoutMatchingMode(LayoutMatchingMode.MatchSensitiveOnly);
                    } else {
                        if(!algo.equalsIgnoreCase("ALL")) {
                            System.err.println("Invalid layout matching mode");
                            return false;
                        }

                        config.setLayoutMatchingMode(LayoutMatchingMode.MatchAll);
                    }

                    i += 2;
                } else if(args[i].equalsIgnoreCase("--aliasflowins")) {
                    config.setFlowSensitiveAliasing(false);
                    ++i;
                } else if(args[i].equalsIgnoreCase("--paths")) {
                    config.setComputeResultPaths(true);
                    ++i;
                } else if(args[i].equalsIgnoreCase("--nopaths")) {
                    config.setComputeResultPaths(false);
                    ++i;
                } else if(args[i].equalsIgnoreCase("--aggressivetw")) {
                    aggressiveTaintWrapper = false;
                    ++i;
                } else if(args[i].equalsIgnoreCase("--pathalgo")) {
                    algo = args[i + 1];
                    if(algo.equalsIgnoreCase("CONTEXTSENSITIVE")) {
                        config.setPathBuilder(PathBuilder.ContextSensitive);
                    } else if(algo.equalsIgnoreCase("CONTEXTINSENSITIVE")) {
                        config.setPathBuilder(PathBuilder.ContextInsensitive);
                    } else {
                        if(!algo.equalsIgnoreCase("SOURCESONLY")) {
                            System.err.println("Invalid path reconstruction algorithm");
                            return false;
                        }

                        config.setPathBuilder(PathBuilder.ContextInsensitiveSourceFinder);
                    }

                    i += 2;
                } else if(args[i].equalsIgnoreCase("--summarypath")) {
                    summaryPath = args[i + 1];
                    i += 2;
                } else if(args[i].equalsIgnoreCase("--saveresults")) {
                    resultFilePath = args[i + 1];
                    i += 2;
                } else if(args[i].equalsIgnoreCase("--sysflows")) {
                    config.setIgnoreFlowsInSystemPackages(false);
                    ++i;
                } else if(args[i].equalsIgnoreCase("--notaintwrapper")) {
                    noTaintWrapper = true;
                    ++i;
                } else if(args[i].equalsIgnoreCase("--repeatcount")) {
                    repeatCount = Integer.parseInt(args[i + 1]);
                    i += 2;
                } else if(args[i].equalsIgnoreCase("--noarraysize")) {
                    config.setEnableArraySizeTainting(false);
                    ++i;
                } else if(args[i].equalsIgnoreCase("--arraysize")) {
                    config.setEnableArraySizeTainting(true);
                    ++i;
                } else if(args[i].equalsIgnoreCase("--notypetightening")) {
                    InfoflowAndroidConfiguration.setUseTypeTightening(false);
                    ++i;
                } else if(args[i].equalsIgnoreCase("--safemode")) {
                    InfoflowAndroidConfiguration.setUseThisChainReduction(false);
                    ++i;
                } else if(args[i].equalsIgnoreCase("--logsourcesandsinks")) {
                    config.setLogSourcesAndSinks(true);
                    ++i;
                } else if(args[i].equalsIgnoreCase("--callbackanalyzer")) {
                    algo = args[i + 1];
                    if(algo.equalsIgnoreCase("DEFAULT")) {
                        config.setCallbackAnalyzer(CallbackAnalyzer.Default);
                    } else {
                        if(!algo.equalsIgnoreCase("FAST")) {
                            System.err.println("Invalid callback analysis algorithm");
                            return false;
                        }

                        config.setCallbackAnalyzer(CallbackAnalyzer.Fast);
                    }

                    i += 2;
                } else {
                    ++i;
                }
            }
        }

        return true;
    }

    private static boolean validateAdditionalOptions() {
        if(timeout > 0 && sysTimeout > 0) {
            return false;
        } else if(!config.getFlowSensitiveAliasing() && config.getCallgraphAlgorithm() != CallgraphAlgorithm.OnDemand && config.getCallgraphAlgorithm() != CallgraphAlgorithm.AutomaticSelection) {
            System.err.println("Flow-insensitive aliasing can only be configured for callgraph algorithms that support this choice.");
            return false;
        } else {
            return true;
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        runAnalysisForResults(args, entryPoint, androidManifest, resDirectory);
    }

    private static InfoflowResults runAnalysisTimeout(final String fileName, final String androidJar) {
        FutureTask task = new FutureTask(new Callable() {
            public InfoflowResults call() throws Exception {
                long beforeRun = System.nanoTime();
                InfoflowResults res = Test.runAnalysis(fileName, androidJar);
                Test.infoResults = res;
                return res;
            }
        });
        InfoFlowComputationTimeOut = false;
        ExecutorService executor = Executors.newFixedThreadPool(1);
        executor.execute(task);

        try {
            System.out.println("Running infoflow task...");
            task.get(timeout, TimeUnit.MINUTES);
        } catch (ExecutionException var5) {
            System.err.println("Infoflow computation failed: " + var5.getMessage());
            var5.printStackTrace();
            InfoFlowComputationTimeOut = true;
            infoResults = null;
        } catch (TimeoutException var6) {
            System.err.println("Infoflow computation timed out: " + var6.getMessage());
            var6.printStackTrace();
            InfoFlowComputationTimeOut = true;
            infoResults = null;
        } catch (InterruptedException var7) {
            System.err.println("Infoflow computation interrupted: " + var7.getMessage());
            var7.printStackTrace();
            InfoFlowComputationTimeOut = true;
            infoResults = null;
        }
        task.cancel(true);
        executor.shutdown();
        return infoResults;
    }

    private static void runAnalysisSysTimeout(String fileName, String androidJar) {
        String classpath = System.getProperty("java.class.path");
        String javaHome = System.getProperty("java.home");
        String executable = "/usr/bin/timeout";
        String[] command = new String[]{executable, "-s", "KILL", sysTimeout + "m", javaHome + "/bin/java", "-cp", classpath, "soot.jimple.infoflow.android.TestApps.Test", fileName, androidJar, config.getStopAfterFirstFlow()?"--singleflow":"--nosingleflow", config.getEnableImplicitFlows()?"--implicit":"--noimplicit", config.getEnableStaticFieldTracking()?"--static":"--nostatic", "--aplength", Integer.toString(InfoflowAndroidConfiguration.getAccessPathLength()), "--cgalgo", callgraphAlgorithmToString(config.getCallgraphAlgorithm()), config.getEnableCallbacks()?"--callbacks":"--nocallbacks", config.getEnableExceptionTracking()?"--exceptions":"--noexceptions", "--layoutmode", layoutMatchingModeToString(config.getLayoutMatchingMode()), config.getFlowSensitiveAliasing()?"--aliasflowsens":"--aliasflowins", config.getComputeResultPaths()?"--paths":"--nopaths", aggressiveTaintWrapper?"--aggressivetw":"--nonaggressivetw", "--pathalgo", pathAlgorithmToString(config.getPathBuilder()), summaryPath != null && !summaryPath.isEmpty()?"--summarypath":"", summaryPath != null && !summaryPath.isEmpty()?summaryPath:"", resultFilePath != null && !resultFilePath.isEmpty()?"--saveresults":"", noTaintWrapper?"--notaintwrapper":"", config.getEnableArraySizeTainting()?"":"--noarraysize", InfoflowAndroidConfiguration.getUseTypeTightening()?"":"--notypetightening", InfoflowAndroidConfiguration.getUseThisChainReduction()?"":"--safemode", config.getLogSourcesAndSinks()?"--logsourcesandsinks":"", "--callbackanalyzer", callbackAlgorithmToString(config.getCallbackAnalyzer())};
        System.out.println("Running command: " + executable + " " + Arrays.toString(command));

        try {
            ProcessBuilder ex = new ProcessBuilder(command);
            ex.redirectOutput(new File("out_" + (new File(fileName)).getName() + "_" + repeatCount + ".txt"));
            ex.redirectError(new File("err_" + (new File(fileName)).getName() + "_" + repeatCount + ".txt"));
            Process proc = ex.start();
            proc.waitFor();
        } catch (IOException var8) {
            System.err.println("Could not execute timeout command: " + var8.getMessage());
            var8.printStackTrace();
        } catch (InterruptedException var9) {
            System.err.println("Process was interrupted: " + var9.getMessage());
            var9.printStackTrace();
        }

    }

    private static String callgraphAlgorithmToString(CallgraphAlgorithm algorihm) {
        switch (algorihm) {
            case AutomaticSelection:
                return "AUTO";
            case CHA:
                return "CHA";
            case VTA:
                return "VTA";
            case RTA:
                return "RTA";
            case SPARK:
                return "SPARK";
            case GEOM:
                return "GEOM";
            default:
                return "unknown";
        }
    }

    private static String layoutMatchingModeToString(LayoutMatchingMode mode) {
        switch (mode) {
            case NoMatch:
                return "NONE";
            case MatchSensitiveOnly:
                return "PWD";
            case MatchAll:
                return "ALL";
            default:
                return "unknown";
        }
    }

    private static String pathAlgorithmToString(PathBuilder pathBuilder) {
        switch (pathBuilder) {
            case ContextSensitive:
                return "CONTEXTSENSITIVE";
            case ContextInsensitive :
                return "CONTEXTINSENSITIVE";
            case ContextInsensitiveSourceFinder :
                return "SOURCESONLY";
            default :
                return "UNKNOWN";
        }
    }

    private static String callbackAlgorithmToString(CallbackAnalyzer analyzer) {
        switch (analyzer) {
            case Default:
                return "DEFAULT";
            case Fast:
                return "FAST";
            default :
                return "UNKNOWN";
        }
    }

    private static InfoflowResults runAnalysis(String fileName, String androidJar) {
        try {
            long ex = System.nanoTime();
            SetupApplication app;
            if(null == ipcManager) {
                app = new SetupApplication(androidJar, fileName);
            } else {
                app = new SetupApplication(androidJar, fileName, ipcManager);
            }

            app.setConfig(config);
            if(noTaintWrapper) {
                app.setSootConfig(new IInfoflowConfig() {
                    public void setSootOptions(Options options) {
                        options.set_include_all(true);
                    }
                });
            }

            Object taintWrapper;
            if(noTaintWrapper) {
                taintWrapper = null;
            } else if(summaryPath != null && !summaryPath.isEmpty()) {
                System.out.println("Using the StubDroid taint wrapper");
                taintWrapper = createLibrarySummaryTW();
                if(taintWrapper == null) {
                    System.err.println("Could not initialize StubDroid");
                    return null;
                }
            } else {
                EasyTaintWrapper res;
                if((new File("../soot-infoflow/EasyTaintWrapperSource.txt")).exists()) {
                    res = new EasyTaintWrapper("../soot-infoflow/EasyTaintWrapperSource.txt");
                } else {
                    res = new EasyTaintWrapper("EasyTaintWrapperSource.txt");
                }

                res.setAggressiveMode(aggressiveTaintWrapper);
                taintWrapper = res;
            }

            app.setTaintWrapper((ITaintPropagationWrapper)taintWrapper);

            try {
                if(ExitPointSink) {
                    app.calculateSourcesSinksEntrypoints(ICCExitPointSourceSink.getList());
                } else {
                    app.calculateSourcesSinksEntrypoints(ICCEntryPointSourceSink.getList());
                }
            } catch (XmlPullParserException var9) {
                var9.printStackTrace();
            }

            /*if(DEBUG) {
                app.printEntrypoints();
                app.printSinks();
                app.printSources();
            }*/

            System.out.println("Running data flow analysis...");
            InfoflowResults res1 = app.runInfoflow(new Test.MyResultsAvailableHandler(null));
            System.out.println("Analysis has run for " + (double)(System.nanoTime() - ex) / 1.0E9D + " seconds");
            if(config.getLogSourcesAndSinks()) {
                Iterator var7;
                Stmt s;
                if(!app.getCollectedSources().isEmpty()) {
                    System.out.println("Collected sources:");
                    var7 = app.getCollectedSources().iterator();

                    while(var7.hasNext()) {
                        s = (Stmt)var7.next();
                        System.out.println("\t" + s);
                    }
                }

                if(!app.getCollectedSinks().isEmpty()) {
                    System.out.println("Collected sinks:");
                    var7 = app.getCollectedSinks().iterator();

                    while(var7.hasNext()) {
                        s = (Stmt)var7.next();
                        System.out.println("\t" + s);
                    }
                }
            }

            return res1;
        } catch (IOException var10) {
            System.err.println("Could not read file: " + var10.getMessage());
            var10.printStackTrace();
            throw new RuntimeException(var10);
        }
    }

    public static ITaintPropagationWrapper createLibrarySummaryTW() throws IOException {
        try {
            Class ex = Class.forName("soot.jimple.infoflow.methodSummary.data.summary.LazySummary");
            Object lazySummary = ex.getConstructor(new Class[]{File.class}).newInstance(new Object[]{new File(summaryPath)});
            ITaintPropagationWrapper summaryWrapper = (ITaintPropagationWrapper)Class.forName("soot.jimple.infoflow.methodSummary.taintWrappers.SummaryTaintWrapper").getConstructor(new Class[]{ex}).newInstance(new Object[]{lazySummary});
            ITaintPropagationWrapper systemClassWrapper = new ITaintPropagationWrapper() {
                private ITaintPropagationWrapper wrapper = new EasyTaintWrapper("EasyTaintWrapperSource.txt");

                private boolean isSystemClass(Stmt stmt) {
                    return stmt.containsInvokeExpr()?SystemClassHandler.isClassInSystemPackage(stmt.getInvokeExpr().getMethod().getDeclaringClass().getName()):false;
                }

                public boolean supportsCallee(Stmt callSite) {
                    return this.isSystemClass(callSite) && this.wrapper.supportsCallee(callSite);
                }

                public boolean supportsCallee(SootMethod method) {
                    return SystemClassHandler.isClassInSystemPackage(method.getDeclaringClass().getName()) && this.wrapper.supportsCallee(method);
                }

                public boolean isExclusive(Stmt stmt, Abstraction taintedPath) {
                    return this.isSystemClass(stmt) && this.wrapper.isExclusive(stmt, taintedPath);
                }

                public void initialize(InfoflowManager manager) {
                    this.wrapper.initialize(manager);
                }

                public int getWrapperMisses() {
                    return 0;
                }

                public int getWrapperHits() {
                    return 0;
                }

                public Set<Abstraction> getTaintsForMethod(Stmt stmt, Abstraction d1, Abstraction taintedPath) {
                    return !this.isSystemClass(stmt)?null:this.wrapper.getTaintsForMethod(stmt, d1, taintedPath);
                }

                public Set<Abstraction> getAliasesForMethod(Stmt stmt, Abstraction d1, Abstraction taintedPath) {
                    return !this.isSystemClass(stmt)?null:this.wrapper.getAliasesForMethod(stmt, d1, taintedPath);
                }
            };
            Method setFallbackMethod = summaryWrapper.getClass().getMethod("setFallbackTaintWrapper", new Class[]{ITaintPropagationWrapper.class});
            setFallbackMethod.invoke(summaryWrapper, new Object[]{systemClassWrapper});
            return summaryWrapper;
        } catch (NoSuchMethodException | ClassNotFoundException var5) {
            System.err.println("Could not find library summary classes: " + var5.getMessage());
            var5.printStackTrace();
            return null;
        } catch (InvocationTargetException var6) {
            System.err.println("Could not initialize library summaries: " + var6.getMessage());
            var6.printStackTrace();
            return null;
        } catch (InstantiationException | IllegalAccessException var7) {
            System.err.println("Internal error in library summary initialization: " + var7.getMessage());
            var7.printStackTrace();
            return null;
        }
    }

    private static void printUsage() {
        System.out.println("FlowDroid (c) Secure Software Engineering Group @ EC SPRIDE");
        System.out.println();
        System.out.println("Incorrect arguments: [0] = apk-file, [1] = android-jar-directory");
        System.out.println("Optional further parameters:");
        System.out.println("\t--TIMEOUT n Time out after n seconds");
        System.out.println("\t--SYSTIMEOUT n Hard time out (kill process) after n seconds, Unix only");
        System.out.println("\t--SINGLEFLOW Stop after finding first leak");
        System.out.println("\t--IMPLICIT Enable implicit flows");
        System.out.println("\t--NOSTATIC Disable static field tracking");
        System.out.println("\t--NOEXCEPTIONS Disable exception tracking");
        System.out.println("\t--APLENGTH n Set access path length to n");
        System.out.println("\t--CGALGO x Use callgraph algorithm x");
        System.out.println("\t--NOCALLBACKS Disable callback analysis");
        System.out.println("\t--LAYOUTMODE x Set UI control analysis mode to x");
        System.out.println("\t--ALIASFLOWINS Use a flow insensitive alias search");
        System.out.println("\t--NOPATHS Do not compute result paths");
        System.out.println("\t--AGGRESSIVETW Use taint wrapper in aggressive mode");
        System.out.println("\t--PATHALGO Use path reconstruction algorithm x");
        System.out.println("\t--LIBSUMTW Use library summary taint wrapper");
        System.out.println("\t--SUMMARYPATH Path to library summaries");
        System.out.println("\t--SYSFLOWS Also analyze classes in system packages");
        System.out.println("\t--NOTAINTWRAPPER Disables the use of taint wrappers");
        System.out.println("\t--NOTYPETIGHTENING Disables the use of taint wrappers");
        System.out.println("\t--LOGSOURCESANDSINKS Print out concrete source/sink instances");
        System.out.println("\t--CALLBACKANALYZER x Uses callback analysis algorithm x");
        System.out.println();
        System.out.println("Supported callgraph algorithms: AUTO, CHA, RTA, VTA, SPARK, GEOM");
        System.out.println("Supported layout mode algorithms: NONE, PWD, ALL");
        System.out.println("Supported path algorithms: CONTEXTSENSITIVE, CONTEXTINSENSITIVE, SOURCESONLY");
        System.out.println("Supported callback algorithms: DEFAULT, FAST");
    }

    private static final class MyResultsAvailableHandler implements ResultsAvailableHandler2 {
        private final BufferedWriter wr;

        private MyResultsAvailableHandler() {
            this.wr = null;
        }

        private MyResultsAvailableHandler(BufferedWriter wr) {
            this.wr = wr;
        }

        public void onResultsAvailable(IInfoflowCFG cfg, InfoflowResults results) {
            if(results == null) {
                this.print("No results found.");
            } else {
                results.setInfoflowCFG(cfg);
                Iterator serializer = results.getResults().keySet().iterator();

                while(serializer.hasNext()) {
                    ResultSinkInfo ex = (ResultSinkInfo)serializer.next();
                    this.print("Found a flow to sink " + ex + ", from the following sources:");
                    Iterator var5 = results.getResults().get(ex).iterator();

                    while(var5.hasNext()) {
                        ResultSourceInfo source = (ResultSourceInfo)var5.next();
                        this.print("\t- " + source.getSource() + " (in " + ((SootMethod)cfg.getMethodOf(source.getSource())).getSignature() + ")");
                        if(source.getPath() != null) {
                            this.print("\t\ton Path " + Arrays.toString(source.getPath()));
                        }
                    }
                }

                if(Test.resultFilePath != null && !Test.resultFilePath.isEmpty()) {
                    InfoflowResultsSerializer serializer1 = new InfoflowResultsSerializer(cfg);

                    try {
                        serializer1.serialize(results, Test.resultFilePath);
                    } catch (FileNotFoundException var7) {
                        System.err.println("Could not write data flow results to file: " + var7.getMessage());
                        var7.printStackTrace();
                        throw new RuntimeException(var7);
                    } catch (XMLStreamException var8) {
                        System.err.println("Could not write data flow results to file: " + var8.getMessage());
                        var8.printStackTrace();
                        throw new RuntimeException(var8);
                    }
                }
            }

        }

        private void print(String string) {
            try {
                System.out.println(string);
                if(this.wr != null) {
                    this.wr.write(string + "\n");
                }
            } catch (IOException var3) {
                ;
            }

        }

        public boolean onSingleResultAvailable(ResultSourceInfo source, ResultSinkInfo sinks) {
            this.print("Found a path from " + source.toString() + " to " + sinks.toString() + "\n");
            return true;
        }
    }
}
