//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package edu.psu.cse.siis.ic3;

import edu.psu.cse.siis.coal.arguments.Argument;
import edu.psu.cse.siis.coal.arguments.ArgumentValueAnalysis;
import soot.Scene;
import soot.SootClass;
import soot.Unit;
import soot.Value;
import soot.jimple.Stmt;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class ClassTypeValueAnalysis extends ArgumentValueAnalysis {
    private static final String BROADCAST_RECEIVER = "android.content.BroadcastReceiver";
    private static final String TOP_VALUE = "android.content.BroadcastReceiver";

    public ClassTypeValueAnalysis() {
    }

    public Set<Object> computeArgumentValues(Argument argument, Unit callSite) {
        Stmt stmt = (Stmt)callSite;
        String classType = stmt.getInvokeExpr().getArg(argument.getArgnum()[0]).getType().toString();
        if(!classType.equals("android.content.BroadcastReceiver")) {
            return Collections.singleton(classType);
        } else {
            List subclasses = Scene.v().getActiveHierarchy().getSubclassesOf(Scene.v().getSootClass("android.content.BroadcastReceiver"));
            HashSet subclassStrings = new HashSet();
            Iterator var7 = subclasses.iterator();

            while(var7.hasNext()) {
                SootClass sootClass = (SootClass)var7.next();
                subclassStrings.add(sootClass.getName());
            }

            if(subclassStrings.size() == 0) {
                subclassStrings.add("android.content.BroadcastReceiver");
            }

            return subclassStrings;
        }
    }

    public Set<Object> computeInlineArgumentValues(String[] inlineValue) {
        return new HashSet(Arrays.asList(inlineValue));
    }

    public Object getTopValue() {
        return "android.content.BroadcastReceiver";
    }

    public Set<Object> computeVariableValues(Value value, Stmt callSite) {
        throw new RuntimeException("Should not be reached.");
    }
}
