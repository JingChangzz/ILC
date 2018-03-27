package ilc.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created by Zhangjing on 2/7/2018.
 */
public class UnZipFile {
    private static String jar = "";
    private static String manifest = "";
    private static String arscFile = "";
    private static String resDir = "";

    /**
     * 解压到指定目录
     * rename 解压后的jar、xml文件 rename
     * @param zipPath
     * @param descDir
     */
    public static String unZipFiles(String zipPath, String descDir) throws IOException {
        descDir = new File(zipPath).getParent();
        String zipDir = unZipFiles(new File(zipPath), descDir+"/");
        String name = zipPath.substring(zipPath.lastIndexOf('\\')+1, zipPath.lastIndexOf('.'));
        executeCMD(zipDir);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        File[] zipFiles = new File(zipDir).listFiles();
        for (File f : zipFiles){
            if(f.isFile() && f.getName().equals("classes.jar")){
                jar = zipDir + "/" + name + ".jar";
                f.renameTo(new File(jar));
            }
            if(f.isFile() && f.getName().equals("AndroidManifest.xml")){
                manifest = zipDir + "/" + name + ".xml";
                f.renameTo(new File(manifest));
            }
            if (f.isDirectory() && f.getName().equals("res")){
                resDir = f.getAbsolutePath();
            }
            if(f.isFile() && f.getName().equals("resource.arsc")){
                arscFile = f.getAbsolutePath();
            }
        }
        return zipDir;
    }

    public static void executeCMD(String path){
        String cmd = "aapt package -f -M "+ path +"/AndroidManifest.xml -S "+path +
                "/res -A "+path+"/assets -I E:/gradute/android.jar -F "+path+"/resource.arsc";
        try {
            Runtime.getRuntime().exec(cmd);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 解压文件到指定目录
     * 解压后的文件名，和之前一致
     * @param zipFile   待解压的zip文件
     * @param descDir   指定目录
     */
    @SuppressWarnings("rawtypes")
    public static String unZipFiles(File zipFile, String descDir) throws IOException {

        ZipFile zip = new ZipFile(zipFile, Charset.forName("GBK"));//解决中文文件夹乱码
        String name = zip.getName().substring(zip.getName().lastIndexOf('\\')+1, zip.getName().lastIndexOf('.'));

        File pathFile = new File(descDir+name);
        if (!pathFile.exists()) {
            pathFile.mkdirs();
        }

        for (Enumeration<? extends ZipEntry> entries = zip.entries(); entries.hasMoreElements();) {
            ZipEntry entry = (ZipEntry) entries.nextElement();
            String zipEntryName = entry.getName();
            InputStream in = zip.getInputStream(entry);
            String outPath = (descDir + name +"/"+ zipEntryName).replaceAll("\\*", "/");

            // 判断路径是否存在,不存在则创建文件路径
            File file = new File(outPath.substring(0, outPath.lastIndexOf('/')));
            if (!file.exists()) {
                file.mkdirs();
            }
            // 判断文件全路径是否为文件夹,如果是上面已经上传,不需要解压
            if (new File(outPath).isDirectory()) {
                continue;
            }
            // 输出文件路径信息
//          System.out.println(outPath);

            FileOutputStream out = new FileOutputStream(outPath);
            byte[] buf1 = new byte[1024];
            int len;
            while ((len = in.read(buf1)) > 0) {
                out.write(buf1, 0, len);
            }
            in.close();
            out.close();
        }
        System.out.println("******************解压完毕********************");
        return pathFile.getAbsolutePath();
    }

    public static String getJar() {
        return jar;
    }

    public static void setJar(String jar) {
        UnZipFile.jar = jar;
    }

    public static String getManifest() {
        return manifest;
    }

    public static void setManifest(String manifest) {
        UnZipFile.manifest = manifest;
    }

    public static String getResDir(){
        return resDir;
    }

    public static String getARSC(){
        return arscFile;
    }

    //测试
    public static void main(String[] args) {
        try {
            System.out.println(unZipFiles(new File("E:/gradute/sdk/dataset/upush-3.1.0.aar"), "E:/gradute/sdk/dataset/"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
