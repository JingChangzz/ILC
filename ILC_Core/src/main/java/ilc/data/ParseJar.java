package ilc.data;

import soot.Scene;
import soot.SootClass;
import soot.options.Options;
import soot.util.Chain;
import soot.util.HashChain;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by zhangjing on 12/25/2017.
 *
 * 直接利用soot解析jar，得到类的信息
 */
public class ParseJar {
    public static Set<String> allEntryPoints = new HashSet<>();
    public static Set<String> plainEntryPoints = new HashSet<>();
    public static Set<String> entryPointsForAndroid = new HashSet<>();
    public static Set<String> activityCom = new HashSet<>();
    public static Set<String> serviceCom = new HashSet<>();
    public static Set<String> broadcastCom = new HashSet<>();
    public static Set<String> contentProviderCom = new HashSet<>();
    public static Chain<SootClass> jarClasses = new HashChain<SootClass>();

    public static void main(String[] args) {
        String jarFile = "D:\\Desktop\\sdk\\PushServices\\GETUI_ANDROID_SDK\\GetuiSDK2.10.2.0.jar";
        ParseJar parseJar = new ParseJar();
        parseJar.runParsing(jarFile);
    }

    public void runParsing(String jarFile) {
        initial(jarFile);
        jarClasses = Scene.v().getApplicationClasses();
        for (SootClass clazz : jarClasses) {
            if (clazz.isAbstract() || clazz.isInterface() || clazz.isJavaLibraryClass()){
                continue;
            } else if (clazz.isPublic()){
//                for (SootMethod sm : clazz.getMethods()){
//                    Body b = sm.retrieveActiveBody();
//                    System.out.println(b.toString());
//                }
                String superClass = clazz.getSuperclass().getName();
                allEntryPoints.add(clazz.getName());
                if (superClass.contains("android.app.Activity")){
                    entryPointsForAndroid.add(clazz.getName());
                    activityCom.add(clazz.getName());
                    continue;
                } else if(superClass.equals("android.content.BroadcastReceiver")) {
                    entryPointsForAndroid.add(clazz.getName());
                    broadcastCom.add(clazz.getName());
                    continue;
                }else if(superClass.equals("android.content.ContentProvider")) {
                    entryPointsForAndroid.add(clazz.getName());
                    contentProviderCom.add(clazz.getName());
                    continue;
                }else if(superClass.equals("android.app.Service")
                        || superClass.equals("android.app.IntentService")) {
                    entryPointsForAndroid.add(clazz.getName());
                    serviceCom.add(clazz.getName());
                    continue;
                }

                plainEntryPoints.add(clazz.getName());
            }
        }
        System.out.println("jar parseing over");
    }

    private static void initial(String apkPath) {
        soot.G.reset();
        Options.v().set_allow_phantom_refs(true);
        Options.v().set_prepend_classpath(true);
        Options.v().set_validate(true);
        Options.v().set_output_format(Options.output_format_jimple);
        Options.v().set_src_prec(Options.src_prec_only_class);
        Options.v().set_process_dir(Collections.singletonList(apkPath));//路径应为文件夹
        Options.v().set_keep_line_number(true);
//      Options.v().set_whole_program(true);
        Options.v().set_no_bodies_for_excluded(false);
        Options.v().set_app(true);
//       Scene.v().setMainClass(appclass); // how to make it work ?
        Scene.v().addBasicClass("java.io.PrintStream", SootClass.SIGNATURES);
        Scene.v().addBasicClass("java.lang.System", SootClass.SIGNATURES);
        Scene.v().addBasicClass("java.lang.Thread", SootClass.SIGNATURES);
//        Scene.v().setSootClassPath("/Library/Java/JavaVirtualMachines/jdk1.8.0.jdk/Contents/Home/jre/lib/rt.jar");
        Scene.v().loadNecessaryClasses();
    }

    private enum ClassType {
        Activity,
        Service,
        BroadcastReceiver,
        ContentProvider,
        Android,
        Plain
    }
}
