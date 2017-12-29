package ilc.utils;

import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Administrator on 11/13/2017.
 */
public class FileUtil {


    /**
     * 获取指定路径下所有文件夹内指定后缀的文件
     * @param path
     * @param suffix
     */
    public static Collection<File> getAllFileWithSuf(String path, String suffix){
        Collection<File> files = null;
        File rootDir = new File(path);
        if (rootDir.isFile()){
            files.add(rootDir);
            return files;
        }

        String[] extensions = {suffix};
        files = FileUtils.listFiles(rootDir, extensions, true);

//        File[] files = rootDir.listFiles();
//        if (files != null) {
//            for (File f : files) {
//                String fileName = f.getName();
//                if (f.isDirectory()) { // 判断是文件还是文件夹
//                    getAllFileWithSuf(f.getAbsolutePath(), suffix); // 获取文件绝对路径
//                } else if (fileName.endsWith(suffix)) { // 判断文件名是否以.avi结尾
//                    String strFile = f.getAbsolutePath();
//                    System.out.println("---" + strFile);
//                    jarPath.add(f.getAbsolutePath());
//                } else {
//                    continue;
//                }
//            }
//        }
        return files;
    }

    public static List<String> readFile(String fileName) throws IOException {
        List<String> result = new ArrayList<>();
        String line;
        FileReader fr = null;
        BufferedReader br = null;
        try {
            fr = new FileReader(fileName);
            br = new BufferedReader(fr);
            while((line = br.readLine()) != null)
                result.add(line);
        }
        finally {
            if (br != null)
                br.close();
            if (fr != null)
                fr.close();
        }
        return result;
    }
}
