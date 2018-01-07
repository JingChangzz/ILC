//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package soot.jimple.infoflow.android.data;

import soot.SootMethod;
import soot.jimple.infoflow.data.SootMethodAndClass;
import soot.jimple.infoflow.util.SootMethodRepresentationParser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class AndroidMethod extends SootMethodAndClass {
    private Set<String> permissions;
    private boolean isSource = false;
    private boolean isSink = false;
    private boolean isNeitherNor = false;
    private AndroidMethod.CATEGORY category = null;

    public AndroidMethod(String methodName, String returnType, String className) {
        super(methodName, className, returnType, new ArrayList());
        this.permissions = null;
    }

    public AndroidMethod(String methodName, List<String> parameters, String returnType, String className) {
        super(methodName, className, returnType, parameters);
        this.permissions = null;
    }

    public AndroidMethod(String methodName, List<String> parameters, String returnType, String className, Set<String> permissions) {
        super(methodName, className, returnType, parameters);
        this.permissions = permissions;
    }

    public AndroidMethod(SootMethod sm) {
        super(sm);
        this.permissions = null;
    }

    public AndroidMethod(SootMethodAndClass methodAndClass) {
        super(methodAndClass);
        this.permissions = null;
    }

    public Set<String> getPermissions() {
        return this.permissions == null?Collections.emptySet():this.permissions;
    }

    public boolean isSource() {
        return this.isSource;
    }

    public void setSource(boolean isSource) {
        this.isSource = isSource;
    }

    public void addPermission(String permission) {
        if(this.permissions == null) {
            this.permissions = new HashSet();
        }

        this.permissions.add(permission);
    }

    public boolean isSink() {
        return this.isSink;
    }

    public void setSink(boolean isSink) {
        this.isSink = isSink;
    }

    public boolean isNeitherNor() {
        return this.isNeitherNor;
    }

    public void setNeitherNor(boolean isNeitherNor) {
        this.isNeitherNor = isNeitherNor;
    }

    public void setCategory(AndroidMethod.CATEGORY category) {
        this.category = category;
    }

    public AndroidMethod.CATEGORY getCategory() {
        return this.category;
    }

    public String toString() {
        String s = this.getSignature();
        String perm;
        if(this.permissions != null) {
            for(Iterator var2 = this.permissions.iterator(); var2.hasNext(); s = s + " " + perm) {
                perm = (String)var2.next();
            }
        }

        if(this.isSource || this.isSink || this.isNeitherNor) {
            s = s + " ->";
        }

        if(this.isSource) {
            s = s + " _SOURCE_";
        }

        if(this.isSink) {
            s = s + " _SINK_ ";
        }

        if(this.isNeitherNor) {
            s = s + " _NONE_";
        }

        if(this.category != null) {
            s = s + "|" + this.category;
        }

        return s;
    }

    public String getSignatureAndPermissions() {
        String s = this.getSignature();
        String perm;
        if(this.permissions != null) {
            for(Iterator var2 = this.permissions.iterator(); var2.hasNext(); s = s + " " + perm) {
                perm = (String)var2.next();
            }
        }

        return s;
    }

    public boolean isAnnotated() {
        return this.isSource || this.isSink || this.isNeitherNor;
    }

    public static AndroidMethod createFromSignature(String signature) {
        if(!signature.startsWith("<")) {
            signature = "<" + signature;
        }

        if(!signature.endsWith(">")) {
            signature = signature + ">";
        }

        SootMethodAndClass smac = SootMethodRepresentationParser.v().parseSootMethodString(signature);
        return new AndroidMethod(smac.getMethodName(), smac.getParameters(), smac.getReturnType(), smac.getClassName());
    }

    public int hashCode() {
        boolean prime = true;
        int result = super.hashCode();
        result = 31 * result + (this.category == null?0:this.category.hashCode());
        result = 31 * result + (this.isNeitherNor?1231:1237);
        result = 31 * result + (this.isSink?1231:1237);
        result = 31 * result + (this.isSource?1231:1237);
        result = 31 * result + (this.permissions == null?0:this.permissions.hashCode());
        return result;
    }

    public boolean equals(Object obj) {
        if(this == obj) {
            return true;
        } else if(!super.equals(obj)) {
            return false;
        } else if(this.getClass() != obj.getClass()) {
            return false;
        } else {
            AndroidMethod other = (AndroidMethod)obj;
            if(this.category != other.category) {
                return false;
            } else if(this.isNeitherNor != other.isNeitherNor) {
                return false;
            } else if(this.isSink != other.isSink) {
                return false;
            } else if(this.isSource != other.isSource) {
                return false;
            } else {
                if(this.permissions == null) {
                    if(other.permissions != null) {
                        return false;
                    }
                } else if(!this.permissions.equals(other.permissions)) {
                    return false;
                }

                return true;
            }
        }
    }

    public static enum CATEGORY {
        ALL,
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
        AUDIO,
        SMS_MMS,
        CONTACT_INFORMATION,
        CALENDAR_INFORMATION,
        SYSTEM_SETTINGS,
        IMAGE,
        BROWSER_INFORMATION,
        NFC;

        private CATEGORY() {
        }
    }
}
