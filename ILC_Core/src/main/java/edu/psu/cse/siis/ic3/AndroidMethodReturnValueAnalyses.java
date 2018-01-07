//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package edu.psu.cse.siis.ic3;

import edu.psu.cse.siis.coal.arguments.LanguageConstraints.Call;
import edu.psu.cse.siis.coal.arguments.MethodReturnValueAnalysis;
import edu.psu.cse.siis.coal.arguments.MethodReturnValueManager;
import soot.Scene;

import java.util.Collections;
import java.util.Set;

public class AndroidMethodReturnValueAnalyses {
    public AndroidMethodReturnValueAnalyses() {
    }

    public static void registerAndroidMethodReturnValueAnalyses(final String appName) {
        MethodReturnValueManager.v().registerMethodReturnValueAnalysis("java.lang.String getPackageName()", new MethodReturnValueAnalysis() {
            public Set<Object> computeMethodReturnValues(Call call) {
                return Scene.v().getActiveHierarchy().isClassSubclassOfIncluding(call.stmt.getInvokeExpr().getMethod().getDeclaringClass(), Scene.v().getSootClass("android.content.Context"))?Collections.singleton(appName):null;
            }
        });
    }
}
