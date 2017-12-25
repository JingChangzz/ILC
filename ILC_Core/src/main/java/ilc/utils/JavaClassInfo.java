package ilc.utils;

import java.io.File;
import java.util.List;

/**
 * Created by Administrator on 12/19/2017.
 */
public class JavaClassInfo {
    private String path;
    private String className;
    private List<String> allMethods;

    public JavaClassInfo(File in){
        setPath(in.getAbsolutePath());
        setClassName(in.getName().split(".java")[0]);
        setAllMethods(JavaAnalysis.getAllMethodsOfJava(getPath()));
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public List<String> getAllMethods() {
        return allMethods;
    }

    public void setAllMethods(List<String> allMethods) {
        this.allMethods = allMethods;
    }

}
