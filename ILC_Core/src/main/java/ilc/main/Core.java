package ilc.main;

import edu.psu.cse.siis.ic3.Ic3Main;
import edu.psu.cse.siis.ic3.Timers;
import ilc.data.ParseJar;
import ilc.db.DbInit;
import ilc.db.ILCSQLConnection;
import soot.SootMethod;
import soot.jimple.Stmt;
import soot.jimple.infoflow.Infoflow;
import soot.jimple.infoflow.SequentialEntryPointCreator;
import soot.jimple.infoflow.android.InfoflowAndroidConfiguration;
import soot.jimple.infoflow.android.TestApps.Test;
import soot.jimple.infoflow.android.data.ICCEntryPointSourceSink;
import soot.jimple.infoflow.android.data.ICCExitPointSourceSink;
import soot.jimple.infoflow.android.data.parsers.PermissionMethodParser;
import soot.jimple.infoflow.cfg.BiDirICFGFactory;
import soot.jimple.infoflow.data.pathBuilders.DefaultPathBuilderFactory;
import soot.jimple.infoflow.data.pathBuilders.DefaultPathBuilderFactory.PathBuilder;
import soot.jimple.infoflow.results.InfoflowResults;
import soot.jimple.infoflow.results.ResultSinkInfo;
import soot.jimple.infoflow.results.ResultSourceInfo;
import soot.jimple.infoflow.source.DefaultSourceSinkManager;
import soot.jimple.infoflow.source.ISourceSinkManager;
import soot.jimple.infoflow.source.data.SourceSinkDefinition;
import soot.jimple.infoflow.taintWrappers.EasyTaintWrapper;
import soot.jimple.infoflow.taintWrappers.ITaintPropagationWrapper;
import soot.tagkit.LineNumberTag;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
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

/**
 * Created by zhangjing on 11/7/2017.
 *
 * 1.直接利用soot分析jar得到类的信息
 *
 *
 * 2.计算数据流
 * exitpath：source->sink
 * entrypath：source->sink
 * 两种路径的souce、sink点定义不同
 *
 */
public class Core {
    static String classPath = "D:/Android/platforms/android-19/android.jar";
    static Set<String> sourcesForExit = new HashSet<>(); // exitpath
    static Set<String> sinksForExit = new HashSet<>();   // exitpath
    static Set<String> sourcesForEntry = new HashSet<>(); // entrypath
    static Set<String> sinksForEntry = new HashSet<>();   // entrypath
    static EasyTaintWrapper taintWrapper;

    static InfoflowResults plainInfoRresults = null;
    static boolean InfoFlowComputationTimeOut = false;

    public static void runPlainAnalysis(String jarFile, Set<String> sources, Set<String> sinks, Set<String> entryPoints, Object taintWrapper) throws IOException {
        //analysis
        PathBuilder pathBuilder = PathBuilder.ContextSensitive;
        Infoflow infoFlow = new Infoflow(classPath, true, (BiDirICFGFactory)null, new DefaultPathBuilderFactory(pathBuilder, true));
        for (String en : entryPoints) {
            System.out.println("Entry point:" + en);
            SequentialEntryPointCreator sepc = new SequentialEntryPointCreator(Collections.singletonList(en));
            ISourceSinkManager ssm = new DefaultSourceSinkManager(sources, sinks, sources, sinks);
            infoFlow.setTaintWrapper((ITaintPropagationWrapper)taintWrapper);
            InfoflowAndroidConfiguration.setAccessPathLength(3);
            plainInfoRresults = plainAnalysisTask(infoFlow, jarFile, sepc, ssm);

            if (InfoFlowComputationTimeOut) {
                System.out.println("Plain Infoflow computation timeout with Context sensitive path builder. Running sourcesonly..");
                pathBuilder = PathBuilder.ContextInsensitiveSourceFinder;
                InfoflowAndroidConfiguration.setAccessPathLength(1);
                infoFlow = new Infoflow(classPath, true, (BiDirICFGFactory)null, new DefaultPathBuilderFactory(pathBuilder, true));
                plainInfoRresults = plainAnalysisTask(infoFlow, jarFile, sepc, ssm);
                InfoFlowComputationTimeOut = true;
            }
            if (plainInfoRresults == null) continue;
            System.out.println(plainInfoRresults.toString());
            System.out.println("============================");
            Iterator serializer = plainInfoRresults.getResults().keySet().iterator();
            while (serializer.hasNext()) {
                ResultSinkInfo ex = (ResultSinkInfo) serializer.next();
                System.out.println("Found a flow to sink " + ex + ", from the following sources:");
                Iterator var5 = plainInfoRresults.getResults().get(ex).iterator();

                while (var5.hasNext()) {
                    ResultSourceInfo source = (ResultSourceInfo) var5.next();
                    System.out.println("\t- " + source.getSource() + " (in " + ((SootMethod)plainInfoRresults.getInfoflowCFG().getMethodOf(source.getSource())).getSignature() + ")");
                    if (source.getPath() != null) {
                        System.out.println("\t\ton Path :");
                        for (Stmt p : source.getPath())
                            System.out.println("\t\t\t" + p.toString());
                    }
                }
            }
            System.out.println("============================");
            if (plainInfoRresults != null) {
                for (ResultSinkInfo sink : plainInfoRresults.getResults().keySet()) { //
                    SootMethod method = plainInfoRresults.getInfoflowCFG().getMethodOf(sink.getSink());
                    System.out.println("sinkInmethod:" + method);
                    String sinkClassName = plainInfoRresults.getInfoflowCFG().getMethodOf(sink.getSink()).getDeclaringClass()
                            .toString();
                    System.out.println("sinkClassName:" + sinkClassName);
                    int instruction = 0;

                    if (sink.getSink().hasTag("LineNumberTag")) {
                        instruction = ((LineNumberTag) sink.getSink().getTag("LineNumberTag")).getLineNumber();
                    }
                    System.out.println("--" + sink.getSink().toString());
                    for (ResultSourceInfo source : plainInfoRresults.getResults().get(sink)) {

                        String leakSource = source.getSource().toString();
                        String methodCalling = null;
                        System.out.println("\t" + leakSource);
                        try {
                            methodCalling = source.getSource().getInvokeExpr().getMethod().getName();
                        } catch (Exception e) {

                        }

                        String leakSink = sink.getSink().toString();
                        StringBuffer leakPath = new StringBuffer();

                        if (source.getPath() != null) {
                            for (Stmt stmt : source.getPath()) {
                                leakPath.append(stmt.toString() + "||");
                            }
                        }
                        System.out.println("exitleakPath---- \t" + leakPath);
                    }
                }
            }
        }
        System.out.println("plain analysis over ---------------->" +jarFile);
    }

    private static InfoflowResults plainAnalysisTask(Infoflow infoFlow, String jarFile, SequentialEntryPointCreator sepc, ISourceSinkManager ssm){
        FutureTask task = new FutureTask(new Callable() {
            public InfoflowResults call() throws Exception {
                long beforeRun = System.nanoTime();
                infoFlow.computeInfoflow(jarFile, classPath, sepc, ssm);
                Core.plainInfoRresults = infoFlow.getResults();
                return Core.plainInfoRresults;
            }
        });

        ExecutorService executor = Executors.newFixedThreadPool(1);
        executor.execute(task);
        try {
            System.out.println("Running plain infoflow task...");
            task.get(3, TimeUnit.MINUTES);
        } catch (ExecutionException var5) {
            System.err.println("Infoflow computation failed: " + var5.getMessage());
            var5.printStackTrace();
            InfoFlowComputationTimeOut = true;
            plainInfoRresults = null;
        } catch (TimeoutException var6) {
            System.err.println("Infoflow computation timed out: " + var6.getMessage());
            var6.printStackTrace();
            InfoFlowComputationTimeOut = true;
            plainInfoRresults = null;
        } catch (InterruptedException var7) {
            System.err.println("Infoflow computation interrupted: " + var7.getMessage());
            var7.printStackTrace();
            InfoFlowComputationTimeOut = true;
            plainInfoRresults = null;
        }
        executor.shutdown();
        return plainInfoRresults;
    }

    private static void runAnalysisForJar(String jarFile, Set<String> entryPoints) throws IOException, InterruptedException, SQLException {
        boolean InfoFlowComputationTimeOut = false;

        //dataflow分析, exit path
        Timers.v().exitPathTimer.start();
        InfoflowResults results = Test.runAnalysisForResults(
                new String[] { jarFile, classPath, "--aplength", "2", "--timeout", "1000000",
                        "--logsourcesandsinks", "--nocallbacks" ,"--pathalgo", "contextsensitive"},
                        entryPoints);

        if (Test.InfoFlowComputationTimeOut) {
            InfoFlowComputationTimeOut = true;
            System.out.println(
                    "Infoflow computation timeout with Context sensitive path builder. Running sourcesonly..");
            results = Test.runAnalysisForResults(new String[] {
                    jarFile, classPath,
                    "--pathalgo", "SOURCESONLY", "--aplength", "1", "--NOPATHS", "--layoutmode", "none",
                    "--aliasflowins", "--noarraysize", "--timeout", "1000000", "--logsourcesandsinks" }, entryPoints);
        }
        runPlainAnalysis(jarFile, sourcesForExit, sinksForExit, entryPoints, taintWrapper);
        Timers.v().exitPathTimer.end();
        if (results != null) {
            for (ResultSinkInfo sink : results.getResults().keySet()) { //
                SootMethod method = results.getInfoflowCFG().getMethodOf(sink.getSink());

                String className = results.getInfoflowCFG().getMethodOf(sink.getSink()).getDeclaringClass()
                        .toString();

                int instruction = 0;

                if (sink.getSink().hasTag("LineNumberTag")) {
                    instruction = ((LineNumberTag) sink.getSink().getTag("LineNumberTag")).getLineNumber();
                }
                System.out.println("--"+sink.getSink().toString());
                for (ResultSourceInfo source : results.getResults().get(sink)) {

                    String leakSource = source.getSource().toString();
                    String methodCalling = null;
                    System.out.println("/t" + leakSource );
                    try {
                        methodCalling = source.getSource().getInvokeExpr().getMethod().getName();
                    } catch (Exception e) {

                    }

                    String leakSink = sink.getSink().toString();
                    StringBuffer leakPath = new StringBuffer();

                    if (source.getPath() != null) {
                        for (Stmt stmt : source.getPath()) {
                            leakPath.append(stmt.toString() + ",");
                        }
                    }

                    System.out.println("leakPath---- \t" + leakPath);
                }
            }
        }
        System.out.println("exitpath over");

        //dataflow分析, entry path
        Timers.v().entryPathTimer.start();
        results = Test.runAnalysisForResults(
                new String[] { jarFile, classPath,
                        "--iccentry", "--aplength", "1", "--timeout",  "1000000", "--logsourcesandsinks"}, entryPoints);

        if (Test.InfoFlowComputationTimeOut) {
            InfoFlowComputationTimeOut = true;
            System.out.println(
                    "Infoflow computation timeout with Context sensitive path builder. Running sourcesonly..");
            results = Test.runAnalysisForResults(new String[] {
                    jarFile, classPath,
                    "--iccentry", "--pathalgo", "SOURCESONLY", "--aplength", "1", "--nopaths", "--layoutmode",
                    "none", "--aliasflowins", "--noarraysize",  "--nostatic", "--logsourcesandsinks",
                    "--timeout", "1000000" }, entryPoints);
        }
        runPlainAnalysis(jarFile, sourcesForEntry, sinksForEntry, entryPoints, taintWrapper);
        Timers.v().entryPathTimer.end();

        if (InfoFlowComputationTimeOut) {
            //DialDroidSQLConnection.markAppTimeout();
        }

    }

    public static void main(String[] args) throws IOException, InterruptedException, SQLException {
        String jarPath = "D:/Desktop/sdk/dataset";
        String dbHost = "localhost";
        String dbName = "ilc";

        //source and sink
        PermissionMethodParser parserExit = PermissionMethodParser.fromStringList(ICCExitPointSourceSink.getList());
        PermissionMethodParser parserEntry = PermissionMethodParser.fromStringList(ICCEntryPointSourceSink.getList());

        Set<SourceSinkDefinition> sinkDefs, sourceDefs;
        sinkDefs = parserExit.getSinks();
        sourceDefs = parserExit.getSources();
        Iterator iteratorSink = sinkDefs.iterator();
        while (iteratorSink.hasNext()){
            sinksForExit.add(iteratorSink.next().toString());
        }
        Iterator iteratorSource = sourceDefs.iterator();
        while(iteratorSource.hasNext()){
            sourcesForExit.add(iteratorSource.next().toString());
        }

        sinkDefs = parserEntry.getSinks();
        sourceDefs = parserEntry.getSources();
        Iterator iteratorSinkE = sinkDefs.iterator();
        while (iteratorSinkE.hasNext()){
            sinksForEntry.add(iteratorSinkE.next().toString());
        }
        Iterator iteratorSourceE = sourceDefs.iterator();
        while(iteratorSourceE.hasNext()){
            sourcesForEntry.add(iteratorSourceE.next().toString());
        }

        //TaintWrapper
        if((new File("../soot-infoflow/EasyTaintWrapperSource.txt")).exists()) {
            taintWrapper = new EasyTaintWrapper("../soot-infoflow/EasyTaintWrapperSource.txt");
        } else {
            taintWrapper = new EasyTaintWrapper("EasyTaintWrapperSource.txt");
        }
        taintWrapper.setAggressiveMode(false);

        ArrayList<String> jarFiles = new ArrayList<>();
        try(DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(jarPath), "*.jar")){ // I think that if you analyze .class files you don't need this step
            for (Path path : stream){
                jarFiles.add(path.toString());
            }
        }catch (IOException e){
            System.out.println("Error loading jar files.");
            e.printStackTrace();
            System.exit(-1);
        }

        DbInit.setDBHost(dbHost);
        System.out.println(new File("db.properties").getAbsoluteFile());
        ILCSQLConnection.init(dbName, "db.properties", null, 3306);

        if (jarFiles.size() == 0){
            System.out.println("no file to analyse");
        }else{
            for (String jar : jarFiles){
                if (!jar.contains("classes.jar")){
                    continue;
                }

                //entry points for infoflow analysis
                ParseJar parseJar = new ParseJar();
                parseJar.runParsing(jar);
                Set<String> entryPoints = parseJar.plainEntryPoints;

                Timers.clear();
                //Ic3 数据提取
                Ic3Main.main(new String[] { "-in", jar, "-cp",
                        classPath, "-db", "./db.properties", "-dbname",
                        "ilc", "-dbhost", "localhost"}, "", parseJar.allEntryPoints);

                if(true)
                    return;

                Timers.v().analysisTimer.start();
                runPlainAnalysis(jar, sourcesForExit, sinksForExit, entryPoints, taintWrapper);
                runPlainAnalysis(jar, sourcesForEntry, sinksForEntry, entryPoints, taintWrapper);
                runAnalysisForJar(jar, parseJar.entryPointsForAndroid);
                Timers.v().analysisTimer.end();
                Timers.v().saveTimeToDb();
            }
        }

        //testForJar();
        //test(true);
        //test("D:/Desktop/sdk/PushServices/GETUI_ANDROID_SDK/out3", "D:/Desktop/sdk/PushServices/GETUI_ANDROID_SDK/");

        System.out.println("over");
    }

}
