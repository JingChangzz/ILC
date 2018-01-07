package ilc.main;

import edu.psu.cse.siis.ic3.Ic3Main;
import ilc.utils.JavaAnalysis;
import ilc.utils.JavaClassInfo;
import soot.SootMethod;
import soot.jimple.Stmt;
import soot.jimple.infoflow.Infoflow;
import soot.jimple.infoflow.android.TestApps.Test;
import soot.jimple.infoflow.entryPointCreators.SequentialEntryPointCreator;
import soot.jimple.infoflow.results.InfoflowResults;
import soot.jimple.infoflow.results.ResultSinkInfo;
import soot.jimple.infoflow.results.ResultSourceInfo;
import soot.jimple.infoflow.source.DefaultSourceSinkManager;
import soot.jimple.infoflow.source.ISourceSinkManager;
import soot.tagkit.LineNumberTag;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Collection;
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

    public static void test(String dePath, String jarPath) throws IOException {
        //entry points
        JavaAnalysis javaAnalysis = new JavaAnalysis(dePath);
        Collection<JavaClassInfo> all = javaAnalysis.getAllJavaClassesInfo();
        Set<String> entryPoints = javaAnalysis.allEntryPoints;

        //source and sink
        SetupForAnalysis.main(null);
        Set<String> sources = SetupForAnalysis.sources;
        Set<String> sinks = SetupForAnalysis.sinks;

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

        //analysis
        Infoflow infoFlow = new Infoflow();
        SequentialEntryPointCreator sepc = new SequentialEntryPointCreator(entryPoints);
        ISourceSinkManager ssm = new DefaultSourceSinkManager(sources, sinks, sources, sinks);
        infoFlow.computeInfoflow(jarPath, libPath.toString(), sepc, ssm);
        InfoflowResults infoflowResults = infoFlow.getResults();

    }

    private static void testForJar() throws IOException, InterruptedException, SQLException {
        //entryPoint
        String outJar = "D:/Desktop/sdk/PushServices/GETUI_ANDROID_SDK/out3";
        JavaAnalysis javaAnalysis = new JavaAnalysis(outJar);
        Collection<JavaClassInfo> all = javaAnalysis.getAllJavaClassesInfo();

        //check jar
        String jarFile = "D:/Desktop/sdk/PushServices/GETUI_ANDROID_SDK/";
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
        Ic3Main.main(new String[] { "-in", jarFile, "-cp",
                classPath, "-db", "./db.properties", "-dbname",
                "ilc", "-dbhost", "localhost" });

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

                    System.out.println("/t" + leakPath);
                }
            }
        }
        System.out.println("exitpath over");

    }

    public static void main(String[] args) throws IOException, InterruptedException, SQLException {
        testForJar();

        //test("D:/Desktop/sdk/PushServices/GETUI_ANDROID_SDK/out3", "D:/Desktop/sdk/PushServices/GETUI_ANDROID_SDK/");


        System.out.println("over");
    }

}
