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
import java.util.List;

/**
 * Created by Administrator on 11/13/2017.
 */
public class JavaAnalysis {

    public static List<String> methods = new ArrayList<String>(); //所有的方法

    public static Collection<File> getAllJavaFile(File in){
        Collection<File> results = FileUtils.listFiles(in, new String[]{"java"}, true);
        return results;
    }

    public static List<String> getAllMethodsOfJava(String path){
        FileInputStream in = null;
        CompilationUnit cu = null;
        try {
            in = new FileInputStream(path);
            cu = JavaParser.parse(in);
            // visit and print the methods names
            new MethodVisitor().visit(cu, null);
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return methods;
    }

    /**
     * Simple visitor implementation for visiting MethodDeclaration nodes.
     */
    static class MethodVisitor extends VoidVisitorAdapter {

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
        String test = "D:\\Desktop\\0921\\IAC\\src\\main\\java\\iac\\utils\\Properties.java";
        List<String> re = JavaAnalysis.getAllMethodsOfJava(test);
        for (String s : re){
            System.out.println("-_-_-"+s);
        }

        getAllJavaFile(new File("D:\\Desktop\\0921\\ILC\\src\\main\\java\\ilc\\"));

    }
}
