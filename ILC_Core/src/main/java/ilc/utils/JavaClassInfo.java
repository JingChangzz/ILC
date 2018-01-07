package ilc.utils;

import com.github.javaparser.ast.body.MethodDeclaration;

import java.util.List;

/**
 * Created by Administrator on 12/19/2017.
 */
public class JavaClassInfo {
    private String path;
    private String className;
    private List<MethodDeclaration> allMethods;
    private String packageName;//文件的package

    private String extendsFrom;

    public JavaClassInfo(){
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

    public String getExtendsFrom() {
        return extendsFrom;
    }

    public void setExtendsFrom(String extendsFrom) {
        this.extendsFrom = extendsFrom;
    }

}
