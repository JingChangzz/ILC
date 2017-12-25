package ilc.utils;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * Created by Administrator on 11/13/2017.
 */
public class JavaAnalysis {

    /**
     * 获取所有类的信息
     * @param in 文件夹路径
     * @return 所有类的信息
     */
    public static Collection<JavaClassInfo> getAllClasses(File in){
        Collection<JavaClassInfo> allClassesInfo = new HashSet<JavaClassInfo>();
        Collection<File> allFiles = getAllJavaFile(in);
        for (File f : allFiles){
            JavaClassInfo javaClassInfo = new JavaClassInfo(f);
            //javaClassInfo.setPath(f.getAbsolutePath());
            allClassesInfo.add(javaClassInfo);
        }
        return allClassesInfo;
    }

    /**
     * 根据文件夹路径得到所有java文件
     * @param in
     * @return
     */
    public static Collection<File> getAllJavaFile(File in){
        Collection<File> results = FileUtils.listFiles(in, new String[]{"java"}, true);
        return results;
    }

    public static List<String> getAllMethodsOfJava(String path){
        FileInputStream in = null;
        CompilationUnit cu = null;
        MethodVisitor methodVisitor = new MethodVisitor();
        try {
            in = new FileInputStream(path);
            cu = JavaParser.parse(in);
            // visit and print the methods names
            methodVisitor.visit(cu, null);
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return methodVisitor.methods;
    }

    /**
     * Simple visitor implementation for visiting MethodDeclaration nodes.
     */
    static class MethodVisitor extends VoidVisitorAdapter {
        public List<String> methods = new ArrayList<String>(); //所有的方法
        @Override
        public void visit(MethodDeclaration n, Object arg) {
            // here you can access the attributes of the method.
            // this method will be called for all methods in this
            // CompilationUnit, including inner class methods
            // System.out.println(n.getName());
            methods.add(n.getNameAsString());
        }
    }

    public static void main(String[] args) {
        String test = "D:\\Documents\\IdeaProjects\\ILC\\ILC_Core\\src\\main\\java\\ilc\\main\\Core.java";
        List<String> re = JavaAnalysis.getAllMethodsOfJava(test);
        for (String s : re){
            System.out.println("-_-_-"+s);
        }

        //getAllJavaFile(new File("D:\\Desktop\\0921\\ILC\\src\\main\\java\\ilc\\"));
        getAllClasses(new File("D:\\Documents\\IdeaProjects\\ILC\\ILC_Core\\src\\main\\java\\ilc\\"));
    }
}
