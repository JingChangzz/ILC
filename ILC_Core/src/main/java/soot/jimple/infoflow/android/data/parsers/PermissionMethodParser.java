//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package soot.jimple.infoflow.android.data.parsers;

import soot.jimple.infoflow.android.data.AndroidMethod;
import soot.jimple.infoflow.source.data.ISourceSinkDefinitionProvider;
import soot.jimple.infoflow.source.data.SourceSinkDefinition;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PermissionMethodParser implements ISourceSinkDefinitionProvider {
    private Set<SourceSinkDefinition> sourceList = null;
    private Set<SourceSinkDefinition> sinkList = null;
    private Set<SourceSinkDefinition> neitherList = null;
    private static final int INITIAL_SET_SIZE = 10000;
    private List<String> data;
    private final String regex = "^<(.+):\\s*(.+)\\s+(.+)\\s*\\((.*)\\)>\\s*(.*?)(\\s+->\\s+(.*))?$";
    private final String regexNoRet = "^<(.+):\\s*(.+)\\s*\\((.*)\\)>\\s*(.*?)?(\\s+->\\s+(.*))?$";

    public static PermissionMethodParser fromFile(String fileName) throws IOException {
        PermissionMethodParser pmp = new PermissionMethodParser();
        pmp.readFile(fileName);
        return pmp;
    }

    public static PermissionMethodParser fromStringList(List<String> data) throws IOException {
        PermissionMethodParser pmp = new PermissionMethodParser(data);
        return pmp;
    }

    public PermissionMethodParser() {
    }

    public PermissionMethodParser(List<String> data) {
        this.data = data;
    }

    private void readFile(String fileName) throws IOException {
        this.data = new ArrayList();
        FileReader fr = null;
        BufferedReader br = null;

        try {
            fr = new FileReader(fileName);
            br = new BufferedReader(fr);

            String line;
            while((line = br.readLine()) != null) {
                this.data.add(line);
            }
        } finally {
            if(br != null) {
                br.close();
            }

            if(fr != null) {
                fr.close();
            }

        }

    }

    public Set<SourceSinkDefinition> getSources() {
        if(this.sourceList == null || this.sinkList == null) {
            this.parse();
        }

        return this.sourceList;
    }

    public Set<SourceSinkDefinition> getSinks() {
        if(this.sourceList == null || this.sinkList == null) {
            this.parse();
        }

        return this.sinkList;
    }

    private void parse() {
        this.sourceList = new HashSet(10000);
        this.sinkList = new HashSet(10000);
        this.neitherList = new HashSet(10000);
        Pattern p = Pattern.compile("^<(.+):\\s*(.+)\\s+(.+)\\s*\\((.*)\\)>\\s*(.*?)(\\s+->\\s+(.*))?$");
        Pattern pNoRet = Pattern.compile("^<(.+):\\s*(.+)\\s*\\((.*)\\)>\\s*(.*?)?(\\s+->\\s+(.*))?$");
        Iterator var3 = this.data.iterator();

        while(var3.hasNext()) {
            String line = (String)var3.next();
            if(!line.isEmpty() && !line.startsWith("%")) {
                Matcher m = p.matcher(line);
                if(m.find()) {
                    AndroidMethod mNoRet = this.parseMethod(m, true);
                    SourceSinkDefinition am = new SourceSinkDefinition(mNoRet);
                    if(mNoRet.isSource()) {
                        this.sourceList.add(am);
                    } else if(mNoRet.isSink()) {
                        this.sinkList.add(am);
                    } else if(mNoRet.isNeitherNor()) {
                        this.neitherList.add(am);
                    }
                } else {
                    Matcher mNoRet1 = pNoRet.matcher(line);
                    if(mNoRet1.find()) {
                        AndroidMethod am1 = this.parseMethod(mNoRet1, true);
                        SourceSinkDefinition singleMethod = new SourceSinkDefinition(am1);
                        if(am1.isSource()) {
                            this.sourceList.add(singleMethod);
                        } else if(am1.isSink()) {
                            this.sinkList.add(singleMethod);
                        } else if(am1.isNeitherNor()) {
                            this.neitherList.add(singleMethod);
                        }
                    } else {
                        System.err.println("Line does not match: " + line);
                    }
                }
            }
        }

    }

    public Map<String, Set<String>> parse(List<String> contents) throws IOException{
        Set<String> sources = new HashSet<String>(INITIAL_SET_SIZE);
        Set<String> sinks = new HashSet<String>(INITIAL_SET_SIZE);
        Map<String, Set<String>> result = new HashMap<>();
        result.put("SOURCE", sources);
        result.put("SINK", sinks);

        Pattern p = Pattern.compile(regex);
        Pattern pNoRet = Pattern.compile(regexNoRet);

        for(String line : contents){
            if (line.isEmpty() || line.startsWith("%"))
                continue;
            Matcher m = p.matcher(line);
            if(m.find()) {
                AndroidMethod singleMethod = parseMethod(m, true);
                if (singleMethod.isSink())
                    result.get("SINK").add(singleMethod.getSignature());
                    //(SourceSinkDefinition)singleMethod;
                else
                    result.get("SOURCE").add(singleMethod.getSignature());
            }
            else {
                Matcher mNoRet = pNoRet.matcher(line);
                if(mNoRet.find()) {
                    AndroidMethod singleMethod = parseMethod(mNoRet, false);
                    if (singleMethod.isSink())
                        result.get("SINK").add(singleMethod.getSignature());
                    else
                        result.get("SOURCE").add(singleMethod.getSignature());
                }
                else
                    System.err.println("Line does not match: " + line);
            }
        }

        return result;
    }

    private AndroidMethod parseMethod(Matcher m, boolean hasReturnType) {
        assert m.group(1) != null && m.group(2) != null && m.group(3) != null && m.group(4) != null;

        byte groupIdx = 1;
        int var17 = groupIdx + 1;
        String className = m.group(groupIdx).trim();
        String returnType = "";
        if(hasReturnType) {
            returnType = m.group(var17++).trim();
        }

        String methodName = m.group(var17++).trim();
        ArrayList methodParameters = new ArrayList();
        String params = m.group(var17++).trim();
        if(!params.isEmpty()) {
            String[] classData = params.split(",");
            int permData = classData.length;

            for(int permissions = 0; permissions < permData; ++permissions) {
                String parameter = classData[permissions];
                methodParameters.add(parameter.trim());
            }
        }

        String var18 = "";
        String var19 = "";
        HashSet var20 = new HashSet();
        if(var17 < m.groupCount() && m.group(var17) != null) {
            var19 = m.group(var17);
            if(var19.contains("->")) {
                var18 = var19.replace("->", "").trim();
                var19 = "";
            }

            ++var17;
        }

        int var14;
        int var15;
        String target;
        String[] var21;
        if(!var19.isEmpty()) {
            var21 = var19.split(" ");
            var14 = var21.length;

            for(var15 = 0; var15 < var14; ++var15) {
                target = var21[var15];
                var20.add(target);
            }
        }

        AndroidMethod singleMethod = new AndroidMethod(methodName, methodParameters, returnType, className, var20);
        if(var18.isEmpty() && m.group(var17) != null) {
            var18 = m.group(var17).replace("->", "").trim();
            ++var17;
        }

        if(!var18.isEmpty()) {
            var21 = var18.split("\\s");
            var14 = var21.length;

            for(var15 = 0; var15 < var14; ++var15) {
                target = var21[var15];
                target = target.trim();
                if(target.contains("|")) {
                    target = target.substring(target.indexOf(124));
                }

                if(!target.isEmpty() && !target.startsWith("|")) {
                    if(target.equals("_SOURCE_")) {
                        singleMethod.setSource(true);
                    } else if(target.equals("_SINK_")) {
                        singleMethod.setSink(true);
                    } else {
                        if(!target.equals("_NONE_")) {
                            throw new RuntimeException("error in target definition: " + target);
                        }

                        singleMethod.setNeitherNor(true);
                    }
                }
            }
        }

        return singleMethod;
    }

    public Set<SourceSinkDefinition> getAllMethods() {
        if(this.sourceList == null || this.sinkList == null) {
            this.parse();
        }

        HashSet sourcesSinks = new HashSet(this.sourceList.size() + this.sinkList.size() + this.neitherList.size());
        sourcesSinks.addAll(this.sourceList);
        sourcesSinks.addAll(this.sinkList);
        sourcesSinks.addAll(this.neitherList);
        return sourcesSinks;
    }
}
