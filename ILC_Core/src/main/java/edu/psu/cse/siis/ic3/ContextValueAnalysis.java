//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package edu.psu.cse.siis.ic3;

import edu.psu.cse.siis.coal.arguments.Argument;
import edu.psu.cse.siis.coal.arguments.ArgumentValueAnalysis;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import soot.Unit;
import soot.Value;
import soot.jimple.Stmt;

public class ContextValueAnalysis extends ArgumentValueAnalysis {
    private static final String TOP_VALUE = "(.*)";
    private final String appName;

    public ContextValueAnalysis(String appName) {
        this.appName = appName != null?appName:"(.*)";
    }

    public Set<Object> computeArgumentValues(Argument argument, Unit callSite) {
        return Collections.singleton(this.appName);
    }

    public Set<Object> computeInlineArgumentValues(String[] inlineValue) {
        return new HashSet(Arrays.asList(inlineValue));
    }

    public Object getTopValue() {
        return "(.*)";
    }

    public Set<Object> computeVariableValues(Value value, Stmt callSite) {
        throw new RuntimeException("Should not be reached.");
    }
}
