//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package soot.jimple.infoflow.android;

import soot.jimple.infoflow.InfoflowConfiguration;
import soot.jimple.infoflow.android.source.AndroidSourceSinkManager.LayoutMatchingMode;
import soot.jimple.infoflow.data.pathBuilders.DefaultPathBuilderFactory.PathBuilder;

public class InfoflowAndroidConfiguration extends InfoflowConfiguration {
    private boolean computeResultPaths = true;
    private PathBuilder pathBuilder;
    private boolean enableCallbacks;
    private boolean enableCallbackSources;
    private InfoflowAndroidConfiguration.CallbackAnalyzer callbackAnalyzer;
    private LayoutMatchingMode layoutMatchingMode;

    public InfoflowAndroidConfiguration() {
        this.pathBuilder = PathBuilder.ContextSensitive;
        this.enableCallbacks = true;
        this.enableCallbackSources = false;
        this.callbackAnalyzer = InfoflowAndroidConfiguration.CallbackAnalyzer.Default;
        this.layoutMatchingMode = LayoutMatchingMode.NoMatch;
        this.setEnableArraySizeTainting(false);
        this.setInspectSources(false);
        this.setInspectSinks(false);
    }

    public void merge(InfoflowConfiguration config) {
        super.merge(config);
        if(config instanceof InfoflowAndroidConfiguration) {
            InfoflowAndroidConfiguration androidConfig = (InfoflowAndroidConfiguration)config;
            this.computeResultPaths = androidConfig.computeResultPaths;
            this.pathBuilder = androidConfig.pathBuilder;
            this.enableCallbacks = androidConfig.enableCallbacks;
            this.enableCallbackSources = androidConfig.enableCallbackSources;
            this.layoutMatchingMode = androidConfig.layoutMatchingMode;
        }

    }

    public void setPathBuilder(PathBuilder builder) {
        this.pathBuilder = builder;
    }

    public PathBuilder getPathBuilder() {
        return this.pathBuilder;
    }

    public void setComputeResultPaths(boolean computeResultPaths) {
        this.computeResultPaths = computeResultPaths;
    }

    public boolean getComputeResultPaths() {
        return this.computeResultPaths;
    }

    public void setEnableCallbacks(boolean enableCallbacks) {
        this.enableCallbacks = enableCallbacks;
    }

    public boolean getEnableCallbacks() {
        return this.enableCallbacks;
    }

    public void setEnableCallbackSources(boolean enableCallbackSources) {
        this.enableCallbackSources = enableCallbackSources;
    }

    public boolean getEnableCallbackSources() {
        return this.enableCallbackSources;
    }

    public void setLayoutMatchingMode(LayoutMatchingMode mode) {
        this.layoutMatchingMode = mode;
    }

    public LayoutMatchingMode getLayoutMatchingMode() {
        return this.layoutMatchingMode;
    }

    public void setCallbackAnalyzer(InfoflowAndroidConfiguration.CallbackAnalyzer callbackAnalyzer) {
        this.callbackAnalyzer = callbackAnalyzer;
    }

    public InfoflowAndroidConfiguration.CallbackAnalyzer getCallbackAnalyzer() {
        return this.callbackAnalyzer;
    }

    public static enum CallbackAnalyzer {
        Default,
        Fast;

        private CallbackAnalyzer() {
        }
    }
}
