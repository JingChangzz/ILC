package ilc.main;

import edu.psu.cse.siis.ic3.Ic3Main;
import ilc.data.ParseJar;
import soot.SootMethod;
import soot.jimple.Stmt;
import soot.jimple.infoflow.Infoflow;
import soot.jimple.infoflow.SequentialEntryPointCreator;
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

/**
 * Created by zhangjing on 11/7/2017.
 *
 * 1.批量反编译??
 * jar固定路径 -> source固定路径
 * 根据这个是不是说明可以直接分析jar文件？？！！
 *
 *
 * 2.计算数据流
 * source->sink
 *
 *
 *
 */
public class Core {
    static String classPath = "D:/Android/platforms/android-19/android.jar";

    public static void test(String jarFile, Set<String> sources, Set<String> sinks, Set<String> entryPoints, Object taintWrapper, boolean exitPath) throws IOException {

        //analysis
        PathBuilder pathBuilder = PathBuilder.ContextSensitive;
        Infoflow infoFlow = new Infoflow(classPath, true, (BiDirICFGFactory)null, new DefaultPathBuilderFactory(pathBuilder, true));
        for (String en : entryPoints) {
            System.out.println("Entry point:" + en);
            SequentialEntryPointCreator sepc = new SequentialEntryPointCreator(Collections.singletonList(en));
            ISourceSinkManager ssm = new DefaultSourceSinkManager(sources, sinks, sources, sinks);
            infoFlow.setTaintWrapper((ITaintPropagationWrapper)taintWrapper);
            infoFlow.computeInfoflow(jarFile, classPath, sepc, ssm);
            InfoflowResults results = infoFlow.getResults();
            if (results == null) continue;
            System.out.println(results.toString());
            System.out.println("============================");
            Iterator serializer = results.getResults().keySet().iterator();
            while (serializer.hasNext()) {
                ResultSinkInfo ex = (ResultSinkInfo) serializer.next();
                System.out.println("Found a flow to sink " + ex + ", from the following sources:");
                Iterator var5 = results.getResults().get(ex).iterator();

                while (var5.hasNext()) {
                    ResultSourceInfo source = (ResultSourceInfo) var5.next();
                    System.out.println("\t- " + source.getSource() + " (in " + ((SootMethod) results.getInfoflowCFG().getMethodOf(source.getSource())).getSignature() + ")");
                    if (source.getPath() != null) {
                        System.out.println("\t\ton Path :");
                        for (Stmt p : source.getPath())
                            System.out.println("\t\t\t" + p.toString());
                    }
                }
            }
            System.out.println("============================");
            if (results != null) {
                for (ResultSinkInfo sink : results.getResults().keySet()) { //
                    SootMethod method = results.getInfoflowCFG().getMethodOf(sink.getSink());
                    System.out.println("sinkInmethod:" + method);
                    String sinkClassName = results.getInfoflowCFG().getMethodOf(sink.getSink()).getDeclaringClass()
                            .toString();
                    System.out.println("sinkClassName:" + sinkClassName);
                    int instruction = 0;

                    if (sink.getSink().hasTag("LineNumberTag")) {
                        instruction = ((LineNumberTag) sink.getSink().getTag("LineNumberTag")).getLineNumber();
                    }
                    System.out.println("--" + sink.getSink().toString());
                    for (ResultSourceInfo source : results.getResults().get(sink)) {

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
        System.out.println("over test");
    }

    private static void testForJar(String jarFile, Set<String> entryPoints, boolean exitPath) throws IOException, InterruptedException, SQLException {

        //Ic3 数据提取
        Ic3Main.main(new String[] { "-in", jarFile, "-cp",
                classPath, "-db", "./db.properties", "-dbname",
                "ilc", "-dbhost", "localhost"}, "");

        if(true)
            return;
        //dataflow分析, exit path
        InfoflowResults results = Test.runAnalysisForResults(
                new String[] { jarFile, classPath, "--aplength", "2", "--timeout", "1000000",
                        "--logsourcesandsinks", "--nocallbacks" ,"--pathalgo", "contextsensitive"},
                        entryPoints);

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

    }

    public static void main(String[] args) throws IOException, InterruptedException, SQLException {
        String jarPath = "D:/Desktop/sdk/dataset";

        //source and sink
        Set<String> sourcesExit = new HashSet<>();
        Set<String> sinksExit = new HashSet<>();
        Set<String> sourcesEentry = new HashSet<>();
        Set<String> sinksEntry = new HashSet<>();
        PermissionMethodParser parserExit = PermissionMethodParser.fromStringList(ICCExitPointSourceSink.getList());
        PermissionMethodParser parserEntry = PermissionMethodParser.fromStringList(ICCEntryPointSourceSink.getList());

        Set<SourceSinkDefinition> sinkDefs, sourceDefs;
        sinkDefs = parserExit.getSinks();
        sourceDefs = parserExit.getSources();
        Iterator iteratorSink = sinkDefs.iterator();
        while (iteratorSink.hasNext()){
            sinksExit.add(iteratorSink.next().toString());
        }
        Iterator iteratorSource = sourceDefs.iterator();
        while(iteratorSource.hasNext()){
            sourcesExit.add(iteratorSource.next().toString());
        }

        sinkDefs = parserEntry.getSinks();
        sourceDefs = parserEntry.getSources();
        Iterator iteratorSinkE = sinkDefs.iterator();
        while (iteratorSinkE.hasNext()){
            sinksEntry.add(iteratorSinkE.next().toString());
        }
        Iterator iteratorSourceE = sourceDefs.iterator();
        while(iteratorSourceE.hasNext()){
            sourcesEentry.add(iteratorSourceE.next().toString());
        }

        //TaintWrapper
        EasyTaintWrapper taintWrapper;
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

        if (jarFiles.size() == 0){
            System.out.println("no file to analyse");
        }else{
            for (String jar : jarFiles){

                //entry points
                ParseJar parseJar = new ParseJar();
                parseJar.runParsing(jar);
                Set<String> entryPoints = parseJar.plainEntryPoints;

                test(jar, sourcesExit, sinksExit, entryPoints, taintWrapper, true);
                test(jar, sourcesEentry, sinksEntry, entryPoints, taintWrapper, false);
            }
        }

        //testForJar();
        //test(true);
        //test("D:/Desktop/sdk/PushServices/GETUI_ANDROID_SDK/out3", "D:/Desktop/sdk/PushServices/GETUI_ANDROID_SDK/");

        System.out.println("over");
    }

}
