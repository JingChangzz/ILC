package ilc.Properties;

import ilc.utils.DataSet;

import java.io.File;

/**
 * Created by Administrator on 11/7/2017.
 */
public class JarDeProperties extends Properties {
    private static JarDeProperties jarDeProperties = new JarDeProperties();

    /**
     * 构造基础配置对象
     * 配置文件，可为指定绝对路径的文件
     * 如果无法通过绝对路径加载则在当前工作路径及其子路径下搜索同名文件
     */
    public JarDeProperties() {
        super(new File(DataSet.JarDePropertiesPath));
    }

    /**
     * @return jar文件存放的根路径
     */
    public static String jarPath() {
        return jarDeProperties.getConfiguration().getString("path.jar", null);
    }

    /**
     * @return 反编译后存放文件的位置
     */
    public static String sourcePath() {
        return jarDeProperties.getConfiguration().getString("path.source", null);
    }

    public static void main(String[] args) {
        JarDeProperties jarDeProperties = new JarDeProperties();

        System.out.println(jarDeProperties.jarPath());
        System.out.println(jarDeProperties.sourcePath());
    }

}
