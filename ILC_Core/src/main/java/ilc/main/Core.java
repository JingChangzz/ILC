package ilc.main;

import soot.jimple.infoflow.Infoflow;
import soot.jimple.infoflow.entryPointCreators.SequentialEntryPointCreator;
import soot.jimple.infoflow.results.InfoflowResults;
import soot.jimple.infoflow.source.DefaultSourceSinkManager;
import soot.jimple.infoflow.source.ISourceSinkManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 11/7/2017.
 *
 * 1.批量反编译
 * jar固定路径 -> source固定路径
 *
 * 2.计算数据流
 * source->sink
 *
 *
 *
 */
public class Core {


    public static void main(String[] args) {
        // Sources, sinks and entry points. Use soot representation (e.g. <org.mypackage.MyClass : void method(int)>)
        List<String> sources = new ArrayList<>();
        // I run soot and save the methods I want as sources, sinks or entry points and then put in here
        List<String> sinks = new ArrayList<>();
        List<String> entryPoints = new ArrayList<>();
        // then i run soot-infoflow (which re-runs soots by itself)
        // The application path containing Jars or class files
        String appPath = "E:\\gradute\\lib\\lib-sdksAdvertising\\Facebook-Audience\\4.6.0\\";
        StringBuilder libPath = new StringBuilder();
        try(DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(appPath), "*.jar")){ // I think that if you analyze .class files you don't need this step
            for (Path path : stream){
                libPath.append(path.toFile().getCanonicalFile());
                libPath.append(File.pathSeparator);
            }
        }catch (IOException e){
            System.out.println("Error loading jar files.");
            e.printStackTrace();
            System.exit(-1);
        }
        appPath = libPath.toString();

        // you may want to add path to rt.jar here to libPath
        // libPath.append(System.getProperty("java.home") + "\lib\rt.jar" + Path.pathSeparator);

        // Read the soot thesis for more informations on app classes and lib classes

        Infoflow infoFlow = new Infoflow();
        SequentialEntryPointCreator sepc = new SequentialEntryPointCreator(entryPoints);    // Creates dummy main where calls all the entry points you specify
        ISourceSinkManager ssm = new DefaultSourceSinkManager(sources, sinks, sources, sinks);
        infoFlow.computeInfoflow(appPath, libPath.toString(), sepc, ssm);
        InfoflowResults infoflowResults = infoFlow.getResults();

        // more resources
        // https://soot-build.cs.uni-paderborn.de/public/origin/develop/soot/soot-develop/options/soot_options.htm
        // https://courses.cs.washington.edu/courses/cse501/01wi/project/sable-thesis.pdf
        // https://github.com/Sable/soot/wiki

        // hope it helps and be patient, it takes a while to run in correctly ;)
        System.out.println("over");
    }

}
