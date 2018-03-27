//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package edu.psu.cse.siis.ic3;

import edu.psu.cse.siis.coal.Analysis;
import edu.psu.cse.siis.coal.AnalysisParameters;
import edu.psu.cse.siis.coal.FatalAnalysisException;
import edu.psu.cse.siis.coal.PropagationSceneTransformer;
import edu.psu.cse.siis.coal.PropagationSceneTransformerFilePrinter;
import edu.psu.cse.siis.coal.SymbolFilter;
import edu.psu.cse.siis.coal.arguments.ArgumentValueManager;
import edu.psu.cse.siis.coal.arguments.MethodReturnValueManager;
import edu.psu.cse.siis.coal.field.transformers.FieldTransformerManager;
import edu.psu.cse.siis.ic3.Ic3Data.Application.Builder;
import edu.psu.cse.siis.ic3.db.SQLConnection;
import edu.psu.cse.siis.ic3.manifest.ManifestPullParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import soot.G;
import soot.PackManager;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Transform;
import soot.Value;
import soot.jimple.StaticFieldRef;
import soot.options.Options;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Ic3Analysis extends Analysis<Ic3CommandLineArguments> {
    private static final String INTENT = "android.content.Intent";
    private static final String INTENT_FILTER = "android.content.IntentFilter";
    private static final String BUNDLE = "android.os.Bundle";
    private static final String COMPONENT_NAME = "android.content.ComponentName";
    private static final String ACTIVITY = "android.app.Activity";
    private static final String[] frameworkClassesArray = new String[]{"android.content.Intent", "android.content.IntentFilter", "android.os.Bundle", "android.content.ComponentName", "android.app.Activity"};
    protected static final List<String> frameworkClasses;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private Builder ic3Builder;
    private Map<String, edu.psu.cse.siis.ic3.Ic3Data.Application.Component.Builder> componentNameToBuilderMap;
    protected String outputDir;
    protected Writer writer;
    protected ManifestPullParser detailedManifest;
    protected Map<String, Integer> componentToIdMap;
    protected Ic3SetupApplication ic3SetupApplication;
    public static String packageName;
    protected Ic3CommandLineArguments arguments;
    protected String apkPath;

    protected void registerFieldTransformerFactories(Ic3CommandLineArguments commandLineArguments) {
        Timers.v().totalTimer.start();
        FieldTransformerManager.v().registerDefaultFieldTransformerFactories();
    }

    protected void registerArgumentValueAnalyses(Ic3CommandLineArguments commandLineArguments) {
        ArgumentValueManager.v().registerDefaultArgumentValueAnalyses();
        ArgumentValueManager.v().registerArgumentValueAnalysis("classType", new ClassTypeValueAnalysis());
        ArgumentValueManager.v().registerArgumentValueAnalysis("authority", new AuthorityValueAnalysis());
        ArgumentValueManager.v().registerArgumentValueAnalysis("Set<authority>", new AuthorityValueAnalysis());
        ArgumentValueManager.v().registerArgumentValueAnalysis("path", new PathValueAnalysis());
        ArgumentValueManager.v().registerArgumentValueAnalysis("Set<path>", new PathValueAnalysis());
    }

    protected void registerMethodReturnValueAnalyses(Ic3CommandLineArguments commandLineArguments) {
        MethodReturnValueManager.v().registerDefaultMethodReturnValueAnalyses();
    }

    protected void initializeAnalysis(Ic3CommandLineArguments commandLineArguments) throws FatalAnalysisException {
        long startTime = System.currentTimeMillis() / 1000L;
        this.outputDir = "./ic3output";
        this.componentToIdMap = new HashMap<>();
        String name = new File(commandLineArguments.getManifest()).getName();
        this.packageName = name.substring(0, name.lastIndexOf("."));
        //假如存在manifest文件，处理文件
        if (prepareManifestFile(commandLineArguments, Ic3Main.manifest)) {
            if(commandLineArguments.getProtobufDestination() != null) {
                this.ic3Builder = Ic3Data.Application.newBuilder();
                this.ic3Builder.setAnalysisStart(startTime);
                if(commandLineArguments.getSample() != null) {
                    this.ic3Builder.setSample(commandLineArguments.getSample());
                }
                this.componentNameToBuilderMap = this.detailedManifest.populateProtobuf(this.ic3Builder);
            } else if(commandLineArguments.getDb() != null) {
                SQLConnection.init(commandLineArguments.getDbName(), commandLineArguments.getDb(), commandLineArguments.getSsh(), commandLineArguments.getDbLocalPort());
                this.componentToIdMap = this.detailedManifest.writeToDb(false);
            }
        } else {   //不存在manifest文件
            //根据前面统计的类信息，存入class以及conponent 表

        }

        this.apkPath = commandLineArguments.getInput();
        Timers.v().mainGeneration.start();
        this.ic3SetupApplication = new Ic3SetupApplication(commandLineArguments.getManifest(), this.apkPath, commandLineArguments.getAndroidJar());
        Set entryPointClasses = Ic3Main.entryPointClasses;


//        if(this.detailedManifest == null) {
//            try {
//                ProcessManifest entryPointMap = new ProcessManifest(commandLineArguments.getManifest());
//                entryPointClasses = entryPointMap.getEntryPointClasses();
//                this.packageName = entryPointMap.getPackageName();
//            } catch (XmlPullParserException | IOException var12) {
//                throw new FatalAnalysisException("Could not process manifest file " + commandLineArguments.getManifest() + ": " + var12);
//            }
//        } else {
//            entryPointClasses = this.detailedManifest.getEntryPointClasses();
//            this.packageName = this.detailedManifest.getPackageName();
//        }
//
        Map callBackMethods = new HashMap<>();
        try {
            callBackMethods = this.ic3SetupApplication.calculateSourcesSinksEntrypoints(new HashSet(), new HashSet(), this.packageName, entryPointClasses);
        } catch (IOException var11) {
            this.logger.error("Could not calculate entry points", var11);
            throw new FatalAnalysisException();
        }

        /**
         * 计算entrypoints和callbacks
         * 依旧改成直接处理代码获得（因为不一定有manifest文件和layout文件）
         */

        Timers.v().mainGeneration.end();
        Timers.v().misc.start();
        ArgumentValueManager.v().registerArgumentValueAnalysis("context", new ContextValueAnalysis(this.packageName));
        AndroidMethodReturnValueAnalyses.registerAndroidMethodReturnValueAnalyses(this.packageName);
        if(this.outputDir != null && this.packageName != null) {
            String entryPointMap1 = String.format("%s/%s.csv", new Object[]{this.outputDir, this.packageName});

            try {
                this.writer = new BufferedWriter(new FileWriter(entryPointMap1, false));
            } catch (IOException var10) {
                this.logger.error("Could not open file " + entryPointMap1, var10);
            }
        }

        G.reset();
        HashMap entryPointMap2 = commandLineArguments.computeComponents()?new HashMap():null;
        this.addSceneTransformer(entryPointMap2);
        if(commandLineArguments.computeComponents()) {
            this.addEntryPointMappingSceneTransformer(entryPointClasses, callBackMethods, entryPointMap2);
        }

        Options.v().set_ignore_resolution_errors(true);
        Options.v().set_debug(false);
        Options.v().set_verbose(false);
        Options.v().set_unfriendly_mode(true);
        Options.v().set_no_bodies_for_excluded(false);
        Options.v().set_allow_phantom_refs(true);
        Options.v().set_output_format(12);
        Options.v().set_whole_program(true);
        Options.v().set_soot_classpath(commandLineArguments.getInput());
        Options.v().set_force_android_jar("ic3-android.jar");
        Options.v().set_ignore_resolution_errors(true);
        Options.v().setPhaseOption("cg.spark", "on");
        Options.v().setPhaseOption("jb.ulp", "off");
        Options.v().setPhaseOption("jb.uce", "remove-unreachable-traps:true");
        Options.v().setPhaseOption("cg", "trim-clinit:false");
        Options.v().set_prepend_classpath(true);
        if(AnalysisParameters.v().useShimple()) {
            Options.v().set_via_shimple(true);
            Options.v().set_whole_shimple(true);
        }

        Options.v().set_src_prec(5);
        Timers.v().misc.end();
        Timers.v().classLoading.start();
        Iterator e1 = frameworkClasses.iterator();

        while(e1.hasNext()) {
            String sc = (String)e1.next();
            SootClass c = Scene.v().loadClassAndSupport(sc);
            Scene.v().forceResolve(sc, 3);
            c.setApplicationClass();
        }

        Scene.v().loadNecessaryClasses();
        Timers.v().classLoading.end();
        Timers.v().entryPointMapping.start();
        if (Ic3Main.isPlainEn){
            Scene.v().setEntryPoints(Collections.singletonList(this.ic3SetupApplication.getSequentialEntryPointCreator().createDummyMain()));
        }else {
            Scene.v().setEntryPoints(Collections.singletonList(this.ic3SetupApplication.getEntryPointCreator().createDummyMain()));
        }
        Timers.v().entryPointMapping.end();
        e1 = Scene.v().getClasses().iterator();

        while(e1.hasNext()) {
            SootClass sc1 = (SootClass)e1.next();
            if(sc1.resolvingLevel() == 0) {
                sc1.setResolvingLevel(3);
                sc1.setPhantomClass();
            }
        }

    }

    protected boolean prepareManifestFile(Ic3CommandLineArguments commandLineArguments, String manifestPath) {
        if (!manifestPath.equals("") && manifestPath.endsWith(".xml")) {
            if (commandLineArguments.getDb() != null || commandLineArguments.getProtobufDestination() != null) {
                this.detailedManifest = new ManifestPullParser();
                this.detailedManifest.loadManifestFile(manifestPath);
            }
            return true;
        }else {
            return false;
        }
    }

    protected void prepareManifestFile(Ic3CommandLineArguments commandLineArguments) {
        if(commandLineArguments.getDb() != null || commandLineArguments.getProtobufDestination() != null) {
            this.detailedManifest = new ManifestPullParser();
            this.detailedManifest.loadManifestFile(commandLineArguments.getManifest());
        }

    }

    protected void setApplicationClasses(Ic3CommandLineArguments commandLineArguments) throws FatalAnalysisException {
        AnalysisParameters.v().addAnalysisClasses(this.computeAnalysisClasses(commandLineArguments.getInput()));
        AnalysisParameters.v().addAnalysisClasses(frameworkClasses);
    }

    protected void handleFatalAnalysisException(Ic3CommandLineArguments commandLineArguments, FatalAnalysisException exception) {
        this.logger.error("Could not process application " + this.packageName, exception);
        if(this.outputDir != null && this.packageName != null) {
            try {
                if(this.writer == null) {
                    String e1 = String.format("%s/%s.csv", new Object[]{this.outputDir, this.packageName});
                    this.writer = new BufferedWriter(new FileWriter(e1, false));
                }

                this.writer.write(commandLineArguments.getInput() + " -1\n");
                this.writer.close();
            } catch (IOException var4) {
                this.logger.error("Could not write to file after failure to process application", var4);
            }
        }

    }

    protected void processResults(Ic3CommandLineArguments commandLineArguments) throws FatalAnalysisException {
//        System.out.println("\n*****Manifest*****");
//        System.out.println(this.detailedManifest.toString());
        if(commandLineArguments.getProtobufDestination() != null) {
            ProtobufResultProcessor resultProcessor = new ProtobufResultProcessor();

//            try {
//                resultProcessor.processResult(this.packageName, this.ic3Builder, commandLineArguments.getProtobufDestination(), commandLineArguments.binary(), this.componentNameToBuilderMap, AnalysisParameters.v().getAnalysisClasses().size(), this.writer);
//            } catch (IOException var5) {
//                this.logger.error("Could not process analysis results", var5);
//                throw new FatalAnalysisException();
//            }
        } else {
            ResultProcessor resultProcessor1 = new ResultProcessor();

            try {
                resultProcessor1.processResult(commandLineArguments.getDb() != null, this.packageName, this.componentToIdMap, AnalysisParameters.v().getAnalysisClasses().size(), this.writer);
            } catch (SQLException | IOException var4) {
                this.logger.error("Could not process analysis results", var4);
                throw new FatalAnalysisException();
            }
        }

    }

    protected void finalizeAnalysis(Ic3CommandLineArguments commandLineArguments) {
        /*try {
            SQLConnection.saveAppCategory(commandLineArguments.getAppCategory(), this.apkPath);
            Timers.v().saveTimeToDb();
        } catch (SQLException var3) {
            ;
        }*/

    }

    protected void addSceneTransformer(Map<SootMethod, Set<String>> entryPointMap) {
        Ic3ResultBuilder resultBuilder = new Ic3ResultBuilder();
        resultBuilder.setEntryPointMap(entryPointMap);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String debugDirPath = System.getProperty("user.home") + File.separator + "debug";
        File debugDir = new File(debugDirPath);
        if(!debugDir.exists()) {
            debugDir.mkdir();
        }

        String fileName = dateFormat.format(new Date()) + ".txt";
        String debugFilename = debugDirPath + File.separator + fileName;
        String pack = AnalysisParameters.v().useShimple()?"wstp":"wjtp";
        Transform transform = new Transform(pack + ".ifds", new PropagationSceneTransformer(resultBuilder, new PropagationSceneTransformerFilePrinter(debugFilename, new SymbolFilter() {
            public boolean filterOut(Value symbol) {
                return symbol instanceof StaticFieldRef && ((StaticFieldRef)symbol).getField().getDeclaringClass().getName().startsWith("android.provider");
            }
        })));
        if(PackManager.v().getPack(pack).get(pack + ".ifds") == null) {
            PackManager.v().getPack(pack).add(transform);
        } else {
            Iterator it = PackManager.v().getPack(pack).iterator();

            while(it.hasNext()) {
                Object current = it.next();
                if(current instanceof Transform && ((Transform)current).getPhaseName().equals(pack + ".ifds")) {
                    it.remove();
                    break;
                }
            }

            PackManager.v().getPack(pack).add(transform);
        }

    }

    protected void addEntryPointMappingSceneTransformer(Set<String> entryPointClasses, Map<String, Set<String>> entryPointMapping, Map<SootMethod, Set<String>> entryPointMap) {
        String pack = AnalysisParameters.v().useShimple()?"wstp":"wjtp";
        Transform transform = new Transform(pack + ".epm", new EntryPointMappingSceneTransformer(entryPointClasses, entryPointMapping, entryPointMap));
        if(PackManager.v().getPack(pack).get(pack + ".epm") == null) {
            PackManager.v().getPack(pack).add(transform);
        } else {
            Iterator it = PackManager.v().getPack(pack).iterator();

            while(it.hasNext()) {
                Object current = it.next();
                if(current instanceof Transform && ((Transform)current).getPhaseName().equals(pack + ".epm")) {
                    it.remove();
                    break;
                }
            }

            PackManager.v().getPack(pack).add(transform);
        }

    }

    protected Set<String> computeAnalysisClassesInApk(String apkPath) {
        HashSet result = new HashSet();
        Options.v().set_src_prec(5);
        Options.v().set_ignore_resolution_errors(true);
        Options.v().set_debug(false);
        Options.v().set_verbose(false);
        Options.v().set_unfriendly_mode(true);
        Options.v().set_process_dir(Collections.singletonList(apkPath));
        Options.v().set_allow_phantom_refs(true);
        Options.v().set_force_android_jar(this.arguments.getAndroidJar() + "/android-19/android.jar");
        Scene.v().loadNecessaryClasses();
        Iterator var3 = Scene.v().getApplicationClasses().iterator();

        while(var3.hasNext()) {
            SootClass className = (SootClass)var3.next();
            String name = className.getName();
            if(!name.startsWith("android.app.FragmentManager")) {
                result.add(name);
            }
        }

        G.reset();
        return result;
    }

    public Ic3Analysis(Ic3CommandLineArguments commandLineArguments) {
        this.arguments = commandLineArguments;
    }

    static {
        frameworkClasses = Arrays.asList(frameworkClassesArray);
    }
}
