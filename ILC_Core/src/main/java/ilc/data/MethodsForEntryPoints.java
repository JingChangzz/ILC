package ilc.data;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.ClassHierarchyFactory;
import com.ibm.wala.util.config.AnalysisScopeReader;
import com.ibm.wala.util.io.FileProvider;

import java.io.File;
import java.io.IOException;
/**
 * Created by zhangjing on 12/28/2017.
 */
@Deprecated
public class MethodsForEntryPoints {
    public static void main(String[] args) throws IOException, ClassHierarchyException {
        File exFile=new FileProvider().getFile("./resources/Java60RegressionExclusions.txt");
        AnalysisScope scope = AnalysisScopeReader.makeJavaBinaryAnalysisScope("D:/Desktop/sdk/PushServices/GETUI_ANDROID_SDK/GetuiSDK2.10.2.0.jar", exFile);
        // 构建ClassHierarchy，相当与类的一个层级结构
        ClassHierarchy cha = ClassHierarchyFactory.make(scope);
        for (IClass cl : cha) {
            if(scope.isApplicationLoader(cl.getClassLoader())) {
                for (IMethod m : cl.getAllMethods()) {
                    String ac = "";
                    if (m.isAbstract()) ac = ac + "abstract ";
                    if (m.isClinit()) ac = ac + "clinit ";
                    if (m.isFinal()) ac = ac + "final ";
                    if (m.isInit()) ac = ac + "init ";
                    if (m.isNative()) ac = ac + "native ";
                    if (m.isPrivate()) ac = ac + "private ";
                    if (m.isProtected()) ac = ac + "protected ";
                    if (m.isPublic()) ac = ac + "public ";
                    if (m.isSynchronized()) ac = ac + "synchronized ";
                    if (m.getDeclaringClass().toString().contains("Application,L")) {
                        System.out.println("<" + m.getDeclaringClass().toString() + ": " + m.getName().toString() + ">");
                        System.out.println(m.getReturnType());
                    }
                        //System.out.println(ac + m.getSignature());
                }
            }
        }
    }
}