package ilc.utils;

import org.junit.Test;

import java.util.List;

/**
 * Created by Administrator on 11/13/2017.
 */
public class JavaAnalysisTest {

    @Test
    public void getAllMethodsOfJava() throws Exception {
        String test = "D:\\Desktop\\0921\\IAC\\src\\main\\java\\iac\\utils\\Properties.java";
        List<String> re = JavaAnalysis.getAllMethodsOfJava(test);
        for (String s : re){
            System.out.println("-_-_-"+s);
        }
    }

}