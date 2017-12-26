package ilc.main;

import ilc.data.AndroidMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SetupForAnalysis {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private Set<AndroidMethod> sinks = null;
    private Set<AndroidMethod> sources = null;
    private final Map<String, Set<AndroidMethod>> callbackMethods = new HashMap<String, Set<AndroidMethod>>(10000);


}
