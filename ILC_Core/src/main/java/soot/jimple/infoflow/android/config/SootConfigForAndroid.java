//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package soot.jimple.infoflow.android.config;

import soot.jimple.infoflow.config.IInfoflowConfig;
import soot.options.Options;

import java.util.LinkedList;

public class SootConfigForAndroid implements IInfoflowConfig {
    public SootConfigForAndroid() {
    }

    public void setSootOptions(Options options) {
        LinkedList excludeList = new LinkedList();
        excludeList.add("java.*");
        excludeList.add("sun.misc.*");
        excludeList.add("android.*");
        excludeList.add("org.apache.*");
        excludeList.add("soot.*");
        excludeList.add("javax.servlet.*");
        options.set_exclude(excludeList);
        Options.v().set_no_bodies_for_excluded(true);
        options.set_output_format(12);
    }
}
