package ilc.data;

import soot.SootMethod;
import soot.jimple.infoflow.data.SootMethodAndClass;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class AndroidMethod extends SootMethodAndClass {

    public enum CATEGORY {
        // all categories
        ALL,

        // SOURCES
        NO_CATEGORY,
        HARDWARE_INFO,
        UNIQUE_IDENTIFIER,
        LOCATION_INFORMATION,
        NETWORK_INFORMATION,
        ACCOUNT_INFORMATION,
        EMAIL_INFORMATION,
        FILE_INFORMATION,
        BLUETOOTH_INFORMATION,
        VOIP_INFORMATION,
        DATABASE_INFORMATION,
        PHONE_INFORMATION,

        // SINKS
        PHONE_CONNECTION,
        INTER_APP_COMMUNICATION,
        VOIP,
        PHONE_STATE,
        EMAIL,
        BLUETOOTH,
        ACCOUNT_SETTINGS,
        VIDEO,
        SYNCHRONIZATION_DATA,
        NETWORK,
        EMAIL_SETTINGS,
        FILE,
        LOG,

        // SHARED
        AUDIO,
        SMS_MMS,
        CONTACT_INFORMATION,
        CALENDAR_INFORMATION,
        SYSTEM_SETTINGS,
        IMAGE,
        BROWSER_INFORMATION,
        NFC
    }

    private final Set<String> permissions;

    private boolean isSource = false;
    private boolean isSink = false;
    private boolean isNeitherNor = false;

    private CATEGORY category = null;

    public AndroidMethod(String methodName, String returnType, String className) {
        super(methodName, className, returnType, new ArrayList<String>());
        this.permissions = Collections.emptySet();
    }

    public AndroidMethod(String methodName, List<String> parameters, String returnType, String className) {
        super(methodName, className, returnType, parameters);
        this.permissions = Collections.emptySet();
    }

    public AndroidMethod(String methodName, List<String> parameters, String returnType, String className, Set<String> permissions) {
        super(methodName, className, returnType, parameters);
        this.permissions = permissions;
    }

    public AndroidMethod(SootMethod sm) {
        super(sm);
        this.permissions = Collections.emptySet();
    }

    public AndroidMethod(SootMethodAndClass methodAndClass) {
        super (methodAndClass);
        this.permissions = Collections.emptySet();
    }

    public Set<String> getPermissions() {
        return this.permissions;
    }

    public boolean isSource() {
        return isSource;
    }

    public void setSource(boolean isSource) {
        this.isSource = isSource;
    }

    public void addPermission(String permission){
        this.permissions.add(permission);
    }

    public boolean isSink() {
        return isSink;
    }

    public void setSink(boolean isSink) {
        this.isSink = isSink;
    }

    public boolean isNeitherNor() {
        return isNeitherNor;
    }

    public void setNeitherNor(boolean isNeitherNor) {
        this.isNeitherNor = isNeitherNor;
    }

    public void setCategory(CATEGORY category){
        this.category = category;
    }

    public CATEGORY getCategory() {
        return this.category;
    }

    @Override
    public String toString() {
        String s = getSignature();
        for (String perm : permissions)
            s += " " + perm;

        if (this.isSource || this.isSink || this.isNeitherNor)
            s += " ->";
        if (this.isSource)
            s += " _SOURCE_";
        if (this.isSink)
            s += " _SINK_ ";
        if (this.isNeitherNor)
            s += " _NONE_";

        if (this.category != null)
            s += "|" + category;

        return s;
    }

    public String getSignatureAndPermissions(){
        String s = getSignature();
        for (String perm : permissions)
            s += " " + perm;
        return s;
    }

    /**
     * Gets whether this method has been annotated as a source, sink or
     * neither nor.
     * @return True if there is an annotations for this method, otherwise
     * false.
     */
    public boolean isAnnotated() {
        return isSource || isSink || isNeitherNor;
    }
}

