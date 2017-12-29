package ilc.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by zhangjing on 12/28/2017.
 */
public class DeleteSuper {
    private static final String SPECIALWORLD1 = "super";
    private static final String SPECIALWORLD2 = "this";
    public static void deleteSuperForParser(File file) throws IOException {
        String rl = null;
        StringBuffer bf = new StringBuffer();
        BufferedReader br = new BufferedReader(new FileReader(file));
        while(( rl = br.readLine()) != null)
        {
            if( (rl.trim().startsWith(SPECIALWORLD1)) ||
                    (rl.trim().startsWith(SPECIALWORLD2)) ){
                continue;
            }else{
                bf.append(rl).append("\r\n");
            }
        }
        br.close();

        BufferedWriter out = new BufferedWriter(new FileWriter(file));
        out.write(bf.toString());
        out.flush();
        out.close();
    }
}
