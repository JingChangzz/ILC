//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package edu.psu.cse.siis.ic3;

import edu.psu.cse.siis.coal.arguments.Argument;
import edu.psu.cse.siis.coal.arguments.ArgumentValueAnalysis;
import edu.psu.cse.siis.coal.arguments.ArgumentValueManager;
import edu.psu.cse.siis.ic3.DataAuthority;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import soot.Unit;
import soot.Value;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;

public class AuthorityValueAnalysis extends ArgumentValueAnalysis {
    private static final Object TOP_VALUE = new DataAuthority("(.*)", "(.*)");

    public AuthorityValueAnalysis() {
    }

    public Set<Object> computeArgumentValues(Argument argument, Unit callSite) {
        ArgumentValueAnalysis stringAnalysis = ArgumentValueManager.v().getArgumentValueAnalysis("String");
        Stmt stmt = (Stmt)callSite;
        if(!stmt.containsInvokeExpr()) {
            throw new RuntimeException("Statement " + stmt + " does not contain an invoke expression");
        } else {
            InvokeExpr invokeExpr = stmt.getInvokeExpr();
            Set hosts = stringAnalysis.computeVariableValues(invokeExpr.getArg(argument.getArgnum()[0]), stmt);
            Set ports = stringAnalysis.computeVariableValues(invokeExpr.getArg(argument.getArgnum()[1]), stmt);
            HashSet result = new HashSet();
            Iterator var9 = hosts.iterator();

            while(var9.hasNext()) {
                Object host = var9.next();
                Iterator var11 = ports.iterator();

                while(var11.hasNext()) {
                    Object port = var11.next();
                    result.add(new DataAuthority((String)host, (String)port));
                }
            }

            return result;
        }
    }

    public Set<Object> computeInlineArgumentValues(String[] inlineValue) {
        return new HashSet(Arrays.asList(inlineValue));
    }

    public Object getTopValue() {
        return TOP_VALUE;
    }

    public Set<Object> computeVariableValues(Value value, Stmt callSite) {
        throw new RuntimeException("Should not be reached.");
    }
}
