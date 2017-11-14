package ilc.utils;

import org.junit.Test;

import java.io.File;
import java.util.Collection;

/**
 * Created by Administrator on 11/13/2017.
 */
public class FileUtilTest {

    @Test
    public void getAllFileWithSuf() throws Exception {

        Collection<File> files = FileUtil.getAllFileWithSuf("E:\\gradute\\lib", "jar");
        for (File f:files){
            System.out.println(f.getAbsolutePath());
        }
        System.out.println(files.size());
    }
}