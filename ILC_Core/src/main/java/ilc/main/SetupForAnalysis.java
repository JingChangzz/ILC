package ilc.main;

import ilc.data.AndroidMethod;
import ilc.data.PermissionMethodParser;
import ilc.utils.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Created by zhangjing.
 *
 *
 */
public class SetupForAnalysis {
    private static final int INITIAL_SET_SIZE = 10000;
    private final Logger logger = LoggerFactory.getLogger(getClass());
    public static Set<String> sinks = null;
    public static Set<String> sources = null;
    private Map<String, Set<String>> sourcesAndSinks = null;
    private final Map<String, Set<AndroidMethod>> callbackMethods = new HashMap<String, Set<AndroidMethod>>(INITIAL_SET_SIZE);

    private boolean enableStaticFields = true;
    private boolean enableExceptions = true;
    private boolean enableCallbacks = true;
    private boolean flowSensitiveAliasing = true;
    private boolean computeResultPaths = true;
    private boolean ignoreFlowsInSystemPackages = true;
    private boolean enableCallbackSources = true;

    public void readSourcesSinksFromFile(String sourceSinkFile)throws IOException{
        List<String> sourcesSinks = FileUtil.readFile(sourceSinkFile);
        Map<String, Set<String>> methods = new PermissionMethodParser().parse(sourcesSinks);
        this.sourcesAndSinks = methods;
        this.sources = sourcesAndSinks.get("SOURCE");
        this.sinks = sourcesAndSinks.get("SINK");
    }

    public static void main(String[] args) throws IOException {
        String sourcesAndSinksFile = "./resources/SourcesAndSinks.txt";
        new SetupForAnalysis().readSourcesSinksFromFile(sourcesAndSinksFile);

    }
}
