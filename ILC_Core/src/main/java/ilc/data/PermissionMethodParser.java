package ilc.data;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PermissionMethodParser implements IPermissionMethodParser {

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

    private PermissionMethodParser(List<String> data) {
        this.data = data;
    }

    private void readFile(String fileName) throws IOException{
        String line;
        this.data = new ArrayList<String>();
        FileReader fr = null;
        BufferedReader br = null;
        try {
            fr = new FileReader(fileName);
            br = new BufferedReader(fr);
            while((line = br.readLine()) != null)
                this.data.add(line);
        }
        finally {
            if (br != null)
                br.close();
            if (fr != null)
                fr.close();
        }
    }

    public Set<AndroidMethod> parse() throws IOException{
        Set<AndroidMethod> methodList = new HashSet<AndroidMethod>(INITIAL_SET_SIZE);

        Pattern p = Pattern.compile(regex);
        Pattern pNoRet = Pattern.compile(regexNoRet);

        for(String line : this.data){
            if (line.isEmpty() || line.startsWith("%"))
                continue;
            Matcher m = p.matcher(line);
            if(m.find()) {
                AndroidMethod singleMethod = parseMethod(m, true);
                methodList.add(singleMethod);
            }
            else {
                Matcher mNoRet = pNoRet.matcher(line);
                if(mNoRet.find()) {
                    AndroidMethod singleMethod = parseMethod(mNoRet, false);
                    methodList.add(singleMethod);
                }
                else
                    System.err.println("Line does not match: " + line);
            }
        }

        return methodList;
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
        assert(m.group(1) != null && m.group(2) != null && m.group(3) != null
                && m.group(4) != null);
        AndroidMethod singleMethod;
        int groupIdx = 1;

        //class name
        String className = m.group(groupIdx++).trim();

        String returnType = "";
        if (hasReturnType) {
            //return type
            returnType = m.group(groupIdx++).trim();
        }

        //method name
        String methodName = m.group(groupIdx++).trim();

        //method parameter
        List<String> methodParameters = new ArrayList<String>();
        String params = m.group(groupIdx++).trim();
        if (!params.isEmpty())
            for (String parameter : params.split(","))
                methodParameters.add(parameter.trim());

        //permissions
        String classData = "";
        String permData = "";
        Set<String> permissions = new HashSet<String>();
        if (groupIdx < m.groupCount() && m.group(groupIdx) != null) {
            permData = m.group(groupIdx);
            if (permData.contains("->")) {
                classData = permData.replace("->", "").trim();
                permData = "";
            }
            groupIdx++;
        }
        if (!permData.isEmpty())
            for(String permission : permData.split(" "))
                permissions.add(permission);

        //create method signature
        singleMethod = new AndroidMethod(methodName, methodParameters, returnType, className, permissions);

        if (classData.isEmpty())
            if(m.group(groupIdx) != null) {
                classData = m.group(groupIdx).replace("->", "").trim();
                groupIdx++;
            }
        if (!classData.isEmpty())
            for(String target : classData.split("\\s")) {
                target = target.trim();

                // Throw away categories
                if (target.contains("|"))
                    target = target.substring(target.indexOf('|'));

                if (!target.isEmpty() && !target.startsWith("|")) {
                    if(target.equals("_SOURCE_"))
                        singleMethod.setSource(true);
                    else if(target.equals("_SINK_"))
                        singleMethod.setSink(true);
                    else if(target.equals("_NONE_"))
                        singleMethod.setNeitherNor(true);
                    else
                        throw new RuntimeException("error in target definition: " + target);
                }
            }
        return singleMethod;
    }
}

