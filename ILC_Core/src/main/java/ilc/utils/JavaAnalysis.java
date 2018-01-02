package ilc.utils;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by zhangjing on 11/13/2017.
 */
public class JavaAnalysis {
    public static Collection<JavaClassInfo> allClassesInfo = new HashSet<JavaClassInfo>();
    public static Collection<File> allFiles = null;
    public static Set<String> allEntryPoints = new HashSet<>();

    public JavaAnalysis(String file){
        try {
            getAllJavaClassesInfo(new File(file));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取所有类的信息
     * @param in 文件夹路径
     * @return 所有类的信息
     */
    public static Collection<JavaClassInfo> getAllJavaClassesInfo(File in) throws IOException {
        allFiles = getAllJavaFile(in);
        for (File f : allFiles){
            DeleteSuper.deleteSuperForParser(f);
            JavaClassInfo javaClassInfo = new JavaClassInfo(f);
            allClassesInfo.add(javaClassInfo);
            getEntryPoints();
        }
        return allClassesInfo;
    }

    /**
     * 每个类中的所有public method 生成一个entry point
     * <org.mypackage.MyClass : void method(int)>
     *
     */
    private static void getEntryPoints(){
        Set<String> result = new HashSet<>();
        for (JavaClassInfo info : allClassesInfo){
            if (info.getAllMethods().size() == 0){
                continue;
            }
            for (MethodDeclaration m : info.getAllMethods()) {
                if (m.isAbstract() || m.isPrivate() || m.isProtected()){
                    continue;
                }
                String s = new String();
                s = "<" + info.getPackageName() + "." + info.getClassName() + " : "
                        +  m.getType().toString() + " " + m.getName().toString() +"(";
                for (int i = 0; i < m.getParameters().size(); i++) {
                    if (i > 0){
                        s += ",";
                    }
                    s += m.getParameter(i).getType().toString();
                }
                s += ")>";
                System.out.println("Entrypoints: "+s);
                allEntryPoints.add(s);
                //System.out.println(allEntryPoints.size());
            }
        }
    }

    /**
     * 根据文件夹路径得到所有java文件
     * @param in 根路径
     * @return java文件列表
     */
    public static Collection<File> getAllJavaFile(File in){
        Collection<File> results = FileUtils.listFiles(in, new String[]{"java"}, true);
        return results;
    }

    public static List<MethodDeclaration> getAllMethodsOfJava(String path){
        FileInputStream in = null;
        CompilationUnit cu = null;
        MethodVisitor methodVisitor = new MethodVisitor();
        try {
            in = new FileInputStream(path);
            cu = JavaParser.parse(in);
            // visit and print the methods names
            cu.accept(new MethodVisitor(), null);
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
        public List<MethodDeclaration> methods = new ArrayList<>(); //所有的方法
        @Override
        public void visit(MethodDeclaration n, Object arg) {
            // here you can access the attributes of the method.
            // this method will be called for all methods in this
            // CompilationUnit, including inner class methods
            // System.out.println(n.getName());
            methods.add(n);
        }
    }

    /**
     * 获取java类的package名
     * @param file
     * @return
     */
    public static String getJavaPackageName(String file){
        CompilationUnit cu = null;
        try {
            cu = JavaParser.parse(new File(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        String packageName = cu.getPackageDeclaration().toString().split(";")[0];
        if ("Optional.empty".equals(packageName)){  //存在空文件(反编译失败？)
            return "";
        }else{
            return packageName.substring(17, packageName.length());
        }
    }

    public static void main(String[] args) throws IOException {
//        String test = "ILC_Core/src/main/java/ilc/main/Core.java";
//        List<String> re = JavaAnalysis.getAllMethodsOfJava(test);
//        for (String s : re){
//            System.out.println("-_-_-"+s);
//        }

        //getAllJavaFile(new File("ILC_Core/src/main/java/ilc/"));
        getAllJavaClassesInfo(new File("D:\\Desktop\\sdk\\PushServices\\GETUI_ANDROID_SDK\\out3"));
    }
}
