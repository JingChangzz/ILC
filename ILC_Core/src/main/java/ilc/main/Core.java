package ilc.main;

import edu.psu.cse.siis.ic3.Ic3Main;
import edu.psu.cse.siis.ic3.Timers;
import edu.psu.cse.siis.ic3.db.SQLConnection;
import edu.psu.cse.siis.ic3.db.Table;
import edu.psu.cse.siis.ic3.manifest.SHA256Calculator;
import ilc.data.ParseJar;
import ilc.db.ILCSQLConnection;
import soot.SootMethod;
import soot.Unit;
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
import soot.jimple.infoflow.handlers.ResultsAvailableHandler2;
import soot.jimple.infoflow.results.InfoflowResults;
import soot.jimple.infoflow.results.ResultSinkInfo;
import soot.jimple.infoflow.results.ResultSourceInfo;
import soot.jimple.infoflow.solver.cfg.IInfoflowCFG;
import soot.jimple.infoflow.source.DefaultSourceSinkManager;
import soot.jimple.infoflow.source.ISourceSinkManager;
import soot.jimple.infoflow.source.data.SourceSinkDefinition;
import soot.jimple.infoflow.taintWrappers.EasyTaintWrapper;
import soot.jimple.infoflow.taintWrappers.ITaintPropagationWrapper;
import soot.tagkit.LineNumberTag;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
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

import static ilc.utils.UnZipFile.getJar;
import static ilc.utils.UnZipFile.getManifest;
import static ilc.utils.UnZipFile.unZipFiles;

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
    //
    //  /Users/apple/Library/Android/sdk/platforms/android-19/android.jar
    static String classPath = "D:/Android/platforms/android-19/android.jar";
    static Set<String> sourcesForExit = new HashSet<>(); // exitpath
    static Set<String> sinksForExit = new HashSet<>();   // exitpath
    static Set<String> sourcesForEntry = new HashSet<>(); // entrypath
    static Set<String> sinksForEntry = new HashSet<>();   // entrypath
    static EasyTaintWrapper taintWrapper;

    static InfoflowResults plainInfoRresults = null;
    static boolean InfoFlowComputationTimeOut = false;
    public static int appID = -1;

    public static void runPlainAnalysis(String jarFile, Set<String> sources, Set<String> sinks, Set<String> entryPoints, Object taintWrapper, boolean exitpath) throws IOException, SQLException {
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
                infoFlow.addResultsAvailableHandler(new Core.MyResultsAvailableHandler(null));
                plainInfoRresults = plainAnalysisTask(infoFlow, jarFile, sepc, ssm);
                InfoFlowComputationTimeOut = true;
            }
            if (plainInfoRresults == null) continue;
            if (exitpath){
                //保存 exit path
                saveExitPath(plainInfoRresults);
            }else{
                //保存entry path
                saveEntryPath(plainInfoRresults);
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
                new String[] { jarFile, classPath, "--aplength", "2", "--timeout", "300",
                        "--logsourcesandsinks", "--nocallbacks" ,"--pathalgo", "contextsensitive"},
                        entryPoints);

        if (Test.InfoFlowComputationTimeOut) {
            InfoFlowComputationTimeOut = true;
            System.out.println(
                    "Infoflow computation timeout with Context sensitive path builder. Running sourcesonly..");
            results = Test.runAnalysisForResults(new String[] {
                    jarFile, classPath,
                    "--pathalgo", "SOURCESONLY", "--aplength", "1", "--NOPATHS", "--layoutmode", "none",
                    "--aliasflowins", "--noarraysize", "--timeout", "300", "--logsourcesandsinks" }, entryPoints);
        }
        if (results != null) {
            //保存 exit path
            saveExitPath(results);
        }
        runPlainAnalysis(jarFile, sourcesForExit, sinksForExit, ParseJar.plainEntryPoints, taintWrapper, true);
        Timers.v().exitPathTimer.end();
        System.out.println("exitpath over");

        //dataflow分析, entry path
        Timers.v().entryPathTimer.start();
        results = Test.runAnalysisForResults(
                new String[] { jarFile, classPath,
                        "--iccentry", "--aplength", "1", "--timeout",  "300", "--logsourcesandsinks"}, entryPoints);

        if (Test.InfoFlowComputationTimeOut) {
            InfoFlowComputationTimeOut = true;
            System.out.println(
                    "Infoflow computation timeout with Context sensitive path builder. Running sourcesonly..");
            results = Test.runAnalysisForResults(new String[] {
                    jarFile, classPath,
                    "--iccentry", "--pathalgo", "SOURCESONLY", "--aplength", "1", "--nopaths", "--layoutmode",
                    "none", "--aliasflowins", "--noarraysize",  "--nostatic", "--logsourcesandsinks",
                    "--timeout", "300" }, entryPoints);
        }
        if (results != null){
            //保存entry path
            saveEntryPath(results);
        }
        runPlainAnalysis(jarFile, sourcesForEntry, sinksForEntry, ParseJar.plainEntryPoints, taintWrapper, false);
        Timers.v().entryPathTimer.end();

        if (InfoFlowComputationTimeOut) {
            //DialDroidSQLConnection.markAppTimeout();
        }

    }

    public static void main(String[] args) throws IOException, InterruptedException, SQLException, NoSuchAlgorithmException {
        //
        ///Volumes/ZJ'S PANZI/dataset2018/sdk/dataset
        String jarPath = "E:/gradute/sdk/dataset";
        String dbHost = "localhost";
        String dbName = "111";

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
        File[] files = new File(jarPath).listFiles();
        for (File f : files){
            if (f.isFile() && (f.getAbsolutePath().endsWith(".jar") || f.getAbsolutePath().endsWith(".aar"))){
                jarFiles.add(f.getAbsolutePath());
            }
        }
        /*
        try(DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(jarPath), "*.jar")){ // I think that if you analyze .class files you don't need this step
            for (Path path : stream){
                jarFiles.add(path.toString());
            }
        }catch (IOException e){
            System.out.println("Error loading jar files.");
            e.printStackTrace();
            System.exit(-1);
        }*/

        Table.setDBHost(dbHost);
        System.out.println(new File("db.properties").getAbsoluteFile());
        SQLConnection.init(dbName, "db.properties", null, 3306);

        if (jarFiles.size() == 0){
            System.out.println("no file to analyse");
        }else{
            for (String jar : jarFiles){
                if (!jar.contains("upush")){
                    continue;
                }
                String manifest = "";
                if (isAAR(jar)){
                    String aarDir = unZipFiles(jar, jarPath);
                    jar = getJar();
                    manifest = getManifest();
                }

                String shasum = SHA256Calculator.getSHA256(new File(jar));
                if (!SQLConnection.checkIfAppAnalyzed(shasum)){
                    //SQLConnection.insert(jar.substring(jar.lastIndexOf("\\")+1, jar.length()-1), getVersion(jar), shasum, null, null, null, false);
                }else{
                    //continue;
                }

                //entry points for infoflow analysis
                ParseJar parseJar = new ParseJar();
                parseJar.runParsing(jar);
                Set<String> entryPoints = parseJar.plainEntryPoints;

                Timers.clear();
                //Ic3 数据提取
                if (parseJar.entryPointsForAndroid.size() >0) {
                    Ic3Main.main(new String[]{"-in", jar, "-cp",
                            classPath, "-db", "./db.properties", "-dbname",
                            dbName, "-dbhost", dbHost}, manifest, false);
                }
                Ic3Main.main(new String[] { "-in", jar, "-cp",
                        classPath, "-db", "./db.properties", "-dbname",
                        dbName, "-dbhost", dbHost}, "", true);

//                if(true)
//                    continue;

                Timers.v().analysisTimer.start();

                runAnalysisForJar(jar, parseJar.entryPointsForAndroid);
                Timers.v().analysisTimer.end();
                //Timers.v().saveTimeToDb();
            }
        }

        System.out.println("over");
    }

    private static String getVersion(String file){
        return "0";
    }

    private static boolean isAAR(String file){
        if (file.endsWith(".aar")){
            return true;
        }
        return false;
    }

    private static int getIdForUnit(Unit unit, SootMethod method) {
        int id = 0;
        try {
            for (Unit currentUnit : method.getActiveBody().getUnits()) {
                if (currentUnit == unit) {
                    return id;
                }
                ++id;
            }
        }catch (RuntimeException e){
            return 0;
        }
        return -1;
    }

    private static void saveEntryPath(InfoflowResults results) throws SQLException {
        for (ResultSinkInfo sink : results.getResults().keySet()) { //
            String method = results.getInfoflowCFG().getMethodOf(sink.getSink()).getSignature();
            String className = results.getInfoflowCFG().getMethodOf(sink.getSink()).getDeclaringClass().toString();
            int instruction = getIdForUnit(sink.getSink(), results.getInfoflowCFG().getMethodOf(sink.getSink()));

            if (sink.getSink().hasTag("LineNumberTag")) {
                instruction = ((LineNumberTag) sink.getSink().getTag("LineNumberTag")).getLineNumber();
            }
            for (ResultSourceInfo source : results.getResults().get(sink)) {
                String leakSource = source.getSource().toString();
                String leakSink = sink.getSink().toString();
                StringBuffer leakPath = new StringBuffer();

                if (source.getPath() != null) {
                    for (Stmt stmt : source.getPath()) {
                        leakPath.append(stmt.toString() + ",");
                    }
                }
                System.out.println("entryleakpath----"+leakPath);
                ILCSQLConnection.insertDataEntryLeak(className, method.toString(), instruction,
                        sink.getSink(), leakSource, leakSink, leakPath.toString());
            }
        }
    }

    private static void saveExitPath(InfoflowResults results) throws SQLException {
        for (ResultSinkInfo sink : results.getResults().keySet()) { //
            SootMethod method = results.getInfoflowCFG().getMethodOf(sink.getSink());
            String className = results.getInfoflowCFG().getMethodOf(sink.getSink()).getDeclaringClass().toString();
            int instruction = getIdForUnit(sink.getSink(), results.getInfoflowCFG().getMethodOf(sink.getSink()));

            if (sink.getSink().hasTag("LineNumberTag")) {
                instruction = ((LineNumberTag) sink.getSink().getTag("LineNumberTag")).getLineNumber();
            }
            for (ResultSourceInfo source : results.getResults().get(sink)) {
                String leakSource = source.getSource().toString();
                String methodCalling = null;
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
                System.out.println("exitleakpath----"+leakPath);
                ILCSQLConnection.insertDataExitLeak(className, method.getSignature(), instruction, sink.getSink(),
                        leakSource, leakSink, leakPath.toString(), methodCalling);
            }
        }
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
