//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package edu.psu.cse.siis.ic3;

import edu.psu.cse.siis.coal.AnalysisParameters;
import edu.psu.cse.siis.coal.Result;
import edu.psu.cse.siis.coal.arguments.Argument;
import soot.SootMethod;
import soot.Unit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class Ic3Result extends Result {
    private final Map<SootMethod, Set<String>> entryPointMap;
    private final Map<Unit, Map<Integer, Object>> result = new HashMap();
    private String statistics;

    public Ic3Result(Map<SootMethod, Set<String>> entryPointMap) {
        this.entryPointMap = entryPointMap;
    }

    public Map<Unit, Map<Integer, Object>> getResults() {
        return this.result;
    }

    public Object getResult(Unit unit, Argument argument) {
        Map unitResult = (Map)this.result.get(unit);
        return unitResult != null?unitResult.get(argument.getArgnum()):null;
    }

    public String getStatistics() {
        return this.statistics;
    }

    public Map<SootMethod, Set<String>> getEntryPointMap() {
        return this.entryPointMap;
    }

    public void addResult(Unit unit, int argnum, Object value) {
        Map<Integer, Object> unitResult = (Map)this.result.get(unit);
        if(unitResult == null) {
            unitResult = new HashMap();
            this.result.put(unit, unitResult);
        }

        ((Map)unitResult).put(Integer.valueOf(argnum), value);
    }

    public void setStatistics(String statistics) {
        this.statistics = statistics;
    }

    public void dump() {
        System.out.println("*****Result*****");
        ArrayList results = new ArrayList();
        boolean outputComponents = this.entryPointMap != null;
        Iterator var3 = this.result.entrySet().iterator();

        while(var3.hasNext()) {
            Entry result = (Entry)var3.next();
            Unit unit = (Unit)result.getKey();
            SootMethod method = AnalysisParameters.v().getIcfg().getMethodOf(unit);
            String current = method.getDeclaringClass().getName() + "/" + method.getSubSignature() + " : " + unit + "\n";
            if(outputComponents) {
                Set components = (Set)this.entryPointMap.get(method);
                if(components != null) {
                    current = current + "Components: " + components + "\n";
                } else {
                    current = current + "Unknown components\n";
                }
            }

            Entry entry2;
            for(Iterator components1 = ((Map)result.getValue()).entrySet().iterator(); components1.hasNext(); current = current + "    " + entry2.getKey() + " : " + entry2.getValue() + "\n") {
                entry2 = (Entry)components1.next();
            }

            results.add(current);
        }

        Collections.sort(results);
        var3 = results.iterator();

        while(var3.hasNext()) {
            String result1 = (String)var3.next();
            System.out.println(result1);
        }

    }
}
