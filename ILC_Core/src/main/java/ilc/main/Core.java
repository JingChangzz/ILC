package ilc.main;

import ilc.utils.JavaAnalysis;
import ilc.utils.JavaClassInfo;
import soot.SootMethod;
import soot.jimple.Stmt;
import soot.jimple.infoflow.Infoflow;
import soot.jimple.infoflow.SequentialEntryPointCreator;
import soot.jimple.infoflow.android.TestApps.Test;
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
import soot.tagkit.LineNumberTag;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
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

    public static void test() throws IOException {
        String outJar = "D:\\Desktop\\sdk\\dataset\\devicejar";
        String jarPath = "D:\\Desktop\\sdk\\dataset";
        //entry points
        JavaAnalysis javaAnalysis = new JavaAnalysis(outJar);
        Collection<JavaClassInfo> all = javaAnalysis.getAllJavaClassesInfo();
        Set<String> entryPoints = javaAnalysis.allEntryPoints;


        //check jar
        StringBuilder libPath = new StringBuilder();
        try(DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(jarPath), "*.jar")){ // I think that if you analyze .class files you don't need this step
            for (Path path : stream){
                libPath.append(path.toFile().getCanonicalFile());
                libPath.append(File.pathSeparator);
            }
        }catch (IOException e){
            System.out.println("Error loading jar files.");
            e.printStackTrace();
            System.exit(-1);
        }
        jarPath = libPath.toString();
        System.out.println(jarPath);
        String classPath = "D:/Android/platforms/android-19/android.jar";

        //source and sink
//        SetupForAnalysis.main(null);
//        Set<String> sources = SetupForAnalysis.sources;
//        Set<String> sinks = SetupForAnalysis.sinks;
        Set<String> sources = new HashSet<>();
        Set<String> sinks = new HashSet<>();
        PermissionMethodParser parser = null;
        parser = PermissionMethodParser.fromStringList(ICCExitPointSourceSink.getList());
        Set<SourceSinkDefinition> sinkDefs = parser.getSinks();
        Set<SourceSinkDefinition> sourceDefs = parser.getSources();
        Iterator iteratorSink = sinkDefs.iterator();
        while (iteratorSink.hasNext()){
            sinks.add(iteratorSink.next().toString());
        }
        Iterator iteratorSource = sourceDefs.iterator();
        while(iteratorSource.hasNext()){
            sources.add(iteratorSource.next().toString());
        }


        //analysis
        PathBuilder pathBuilder = PathBuilder.ContextSensitive;
        Infoflow infoFlow = new Infoflow(classPath, true, (BiDirICFGFactory)null, new DefaultPathBuilderFactory(pathBuilder, true));
        SequentialEntryPointCreator sepc = new SequentialEntryPointCreator(entryPoints);
        ISourceSinkManager ssm = new DefaultSourceSinkManager(sources, sinks, sources, sinks);
        infoFlow.computeInfoflow(jarPath, libPath.toString(), sepc, ssm);
        InfoflowResults results = infoFlow.getResults();
        System.out.println(results.toString());
        System.out.println("============================");
        Iterator serializer = results.getResults().keySet().iterator();
        while(serializer.hasNext()) {
            ResultSinkInfo ex = (ResultSinkInfo)serializer.next();
            System.out.println("Found a flow to sink " + ex + ", from the following sources:");
            Iterator var5 = results.getResults().get(ex).iterator();

            while(var5.hasNext()) {
                ResultSourceInfo source = (ResultSourceInfo)var5.next();
                System.out.println("\t- " + source.getSource() + " (in " + ((SootMethod)results.getInfoflowCFG().getMethodOf(source.getSource())).getSignature() + ")");
                if(source.getPath() != null) {
                    System.out.println("\t\ton Path " + Arrays.toString(source.getPath()));
                }
            }
        }
        System.out.println("============================");
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
                    System.out.println("\t" + leakSource );
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

                    System.out.println("leakPath---- \t" + leakPath);
                }
            }
        }
        System.out.println("over test");
    }

    private static void testForJar() throws IOException, InterruptedException, SQLException {
        //entryPoint  D:\\Desktop\\sdk\\dataset\\devicejar
        String outJar = "D:\\Desktop\\sdk\\PushServices\\GETUI_ANDROID_SDK\\out3";
        JavaAnalysis javaAnalysis = new JavaAnalysis(outJar);
        Collection<JavaClassInfo> all = javaAnalysis.getAllJavaClassesInfo();

        //check jar  D:\\Desktop\\sdk\\dataset
        String jarFile = "D:\\Desktop\\sdk\\PushServices\\GETUI_ANDROID_SDK";
        StringBuilder libPath = new StringBuilder();
        try(DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(jarFile), "*.jar")){ // I think that if you analyze .class files you don't need this step
            for (Path path : stream){
                libPath.append(path.toFile().getCanonicalFile());
            }
        }catch (IOException e){
            System.out.println("Error loading jar files.");
            e.printStackTrace();
            System.exit(-1);
        }
        jarFile = libPath.toString();
        System.out.println(jarFile);

        String classPath = "D:/Android/platforms/android-19/android.jar";

        //Ic3 数据提取
//        Ic3Main.main(new String[] { "-in", jarFile, "-cp",
//                classPath, "-db", "./db.properties", "-dbname",
//                "ilc", "-dbhost", "localhost"}, "");
//
//        if(true)
//            return;
        //dataflow分析
        InfoflowResults results = Test.runAnalysisForResults(
                new String[] { jarFile, classPath,
                        "--aplength", "2", "--timeout", "1000000", "--logsourcesandsinks", "--nocallbacks" });

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
        //testForJar();
       test();
        //test("D:/Desktop/sdk/PushServices/GETUI_ANDROID_SDK/out3", "D:/Desktop/sdk/PushServices/GETUI_ANDROID_SDK/");


        System.out.println("over");
    }

}
