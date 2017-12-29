package ilc.utils;

import com.github.javaparser.ast.body.MethodDeclaration;

import java.io.File;
import java.util.List;

/**
 * Created by Administrator on 12/19/2017.
 */
public class JavaClassInfo {
    private String path;
    private String className;
    private List<MethodDeclaration> allMethods;
    private String packageName;//文件的package

    public JavaClassInfo(File in){
        setPath(in.getAbsolutePath());
        setClassName(in.getName().split(".java")[0]);
        setAllMethods(JavaAnalysis.getAllMethodsOfJava(getPath()));
        setPackageName(JavaAnalysis.getJavaPackageName(getPath()));
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

    public List<MethodDeclaration> getAllMethods() {
        return allMethods;
    }

    public void setAllMethods(List<MethodDeclaration> allMethods) {
        this.allMethods = allMethods;
    }


    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }
}
