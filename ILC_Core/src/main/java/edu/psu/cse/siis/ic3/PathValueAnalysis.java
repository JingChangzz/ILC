//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package edu.psu.cse.siis.ic3;

import edu.psu.cse.siis.coal.arguments.Argument;
import edu.psu.cse.siis.coal.arguments.ArgumentValueAnalysis;
import edu.psu.cse.siis.coal.arguments.ArgumentValueManager;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import soot.Unit;
import soot.Value;
import soot.jimple.Stmt;

public class PathValueAnalysis extends ArgumentValueAnalysis {
    private static final String TOP_VALUE = "(.*)";
    private static final int PATTERN_LITERAL = 0;
    private static final int PATTERN_PREFIX = 1;
    private static final int PATTERN_SIMPLE_GLOB = 2;

    public PathValueAnalysis() {
    }

    public Set<Object> computeArgumentValues(Argument argument, Unit callSite) {
        Argument argument0 = new Argument(argument);
        argument0.setArgnum(new int[]{argument.getArgnum()[0]});
        argument0.setType("String");
        Argument argument1 = new Argument(argument);
        argument1.setArgnum(new int[]{argument.getArgnum()[1]});
        argument1.setType("int");
        Set paths = ArgumentValueManager.v().getArgumentValues(argument0, callSite);
        Set types = ArgumentValueManager.v().getArgumentValues(argument1, callSite);
        HashSet result = new HashSet();
        Iterator var8 = paths.iterator();

        while(var8.hasNext()) {
            Object path = var8.next();
            Iterator var10 = types.iterator();

            while(var10.hasNext()) {
                Object type = var10.next();
                result.add(this.computePathForType((String)path, ((Integer)type).intValue()));
            }
        }

        return result;
    }

    private String computePathForType(String path, int type) {
        if(type != 0 && type != 2) {
            if(type != 1 && type != -1) {
                throw new RuntimeException("Unknown path type: " + type);
            } else {
                return path.equals("(.*)")?"(.*)":String.format("%s(.*)", new Object[]{path});
            }
        } else {
            return path;
        }
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
