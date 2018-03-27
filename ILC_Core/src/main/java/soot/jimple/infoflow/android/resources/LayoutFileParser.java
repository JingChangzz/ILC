//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package soot.jimple.infoflow.android.resources;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import soot.PackManager;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.Transform;
import soot.jimple.infoflow.android.axml.AXmlAttribute;
import soot.jimple.infoflow.android.axml.AXmlNode;
import soot.jimple.infoflow.android.resources.ARSCFileParser.AbstractResource;
import soot.jimple.infoflow.android.resources.ARSCFileParser.StringResource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class LayoutFileParser extends AbstractResourceParser {
    private static final boolean DEBUG = true;
    private final Map<String, Set<LayoutControl>> userControls = new HashMap();
    private final Map<String, Set<String>> callbackMethods = new HashMap();
    private final Map<String, Set<String>> includeDependencies = new HashMap();
    private final String packageName;
    private final ARSCFileParser resParser;
    private boolean loadAdditionalAttributes = false;
    private static final int TYPE_NUMBER_VARIATION_PASSWORD = 16;
    private static final int TYPE_TEXT_VARIATION_PASSWORD = 128;
    private static final int TYPE_TEXT_VARIATION_VISIBLE_PASSWORD = 144;
    private static final int TYPE_TEXT_VARIATION_WEB_PASSWORD = 224;

    public LayoutFileParser(String packageName, ARSCFileParser resParser) {
        this.packageName = packageName;
        this.resParser = resParser;
    }

    private boolean isRealClass(SootClass sc) {
        return sc == null?false:!sc.isPhantom() || sc.getMethodCount() != 0 || sc.getFieldCount() != 0;
    }

    private SootClass getLayoutClass(String className) {
        if(className.startsWith(";")) {
            className = className.substring(1);
        }

        if(!className.contains("(") && !className.contains("<") && !className.contains("/")) {
            SootClass sc = Scene.v().forceResolve(className, 3);
            if((sc == null || sc.isPhantom()) && !this.packageName.isEmpty()) {
                sc = Scene.v().forceResolve(this.packageName + "." + className, 3);
            }

            if(!this.isRealClass(sc)) {
                sc = Scene.v().forceResolve("android.view." + className, 3);
            }

            if(!this.isRealClass(sc)) {
                sc = Scene.v().forceResolve("android.widget." + className, 3);
            }

            if(!this.isRealClass(sc)) {
                sc = Scene.v().forceResolve("android.webkit." + className, 3);
            }

            if(!this.isRealClass(sc)) {
                System.err.println("Could not find layout class " + className);
                return null;
            } else {
                return sc;
            }
        } else {
            System.err.println("Invalid class name " + className);
            return null;
        }
    }

    private boolean isLayoutClass(SootClass theClass) {
        if(theClass == null) {
            return false;
        } else {
            boolean found = false;
            Iterator var3 = Scene.v().getActiveHierarchy().getSuperclassesOf(theClass).iterator();

            while(var3.hasNext()) {
                SootClass parent = (SootClass)var3.next();
                if(parent.getName().equals("android.view.ViewGroup")) {
                    found = true;
                    break;
                }
            }

            return found;
        }
    }

    private boolean isViewClass(SootClass theClass) {
        if(theClass == null) {
            return false;
        } else {
            boolean found = false;
            Iterator var3 = Scene.v().getActiveHierarchy().getSuperclassesOfIncluding(theClass).iterator();

            while(var3.hasNext()) {
                SootClass parent = (SootClass)var3.next();
                if(parent.getName().equals("android.view.View") || parent.getName().equals("android.webkit.WebView")) {
                    found = true;
                    break;
                }
            }

            if(!found) {
                System.err.println("Layout class " + theClass.getName() + " is not derived from android.view.View");
                return false;
            } else {
                return true;
            }
        }
    }

    private boolean isAndroidNamespace(String ns) {
        if(ns == null) {
            return false;
        } else {
            ns = ns.trim();
            if(ns.startsWith("*")) {
                ns = ns.substring(1);
            }

            return ns.equals("http://schemas.android.com/apk/res/android");
        }
    }

    private <X, Y> void addToMapSet(Map<X, Set<Y>> target, X layoutFile, Y callback) {
        if(target.containsKey(layoutFile)) {
            ((Set)target.get(layoutFile)).add(callback);
        } else {
            HashSet callbackSet = new HashSet();
            callbackSet.add(callback);
            target.put(layoutFile, callbackSet);
        }

    }

    private void addCallbackMethod(String layoutFile, String callback) {
        this.addToMapSet(this.callbackMethods, layoutFile, callback);
        if(this.includeDependencies.containsKey(layoutFile)) {
            Iterator var3 = ((Set)this.includeDependencies.get(layoutFile)).iterator();

            while(var3.hasNext()) {
                String target = (String)var3.next();
                this.addCallbackMethod(target, callback);
            }
        }

    }

    public void parseLayoutFile(final String fileName) {
        Transform transform = new Transform("wjtp.lfp", new SceneTransformer() {
            protected void internalTransform(String phaseName, Map options) {
                LayoutFileParser.this.parseLayoutFileDirect(fileName);
            }
        });
        PackManager.v().getPack("wjtp").add(transform);
    }

    public void parseLayoutFileDirect(String fileName) {
        Collection<File> allFiles = FileUtils.listFiles(new File(fileName), TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
        for (File f : allFiles){
            if(!f.getName().endsWith(".xml")) {
                System.err.println("Skipping file " + f.getName() + " in layout folder...");
            } else {
                String entryClass = f.getName().substring(0, f.getName().lastIndexOf("."));
                if(!LayoutFileParser.this.packageName.isEmpty()) {
                    entryClass = LayoutFileParser.this.packageName + "." + entryClass;
                }

                FileInputStream stream = null;
                try {
                    stream = new FileInputStream(f);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                /*try {
                    AXmlHandler ex1 = new AXmlHandler(stream, new AXML20Parser());
                    LayoutFileParser.this.parseLayoutNode(f.getAbsolutePath(), ex1.getDocument().getRootNode());
                    System.out.println("Found " + LayoutFileParser.this.userControls.size() + " layout controls in file " + fileName);
                } catch (Exception var8) {
                    System.err.println("Could not read binary XML file: " + var8.getMessage());
                    var8.printStackTrace();
                }*/
            }
        }
    }

    private void parseLayoutNode(String layoutFile, AXmlNode rootNode) {
        if(rootNode.getTag() != null && !rootNode.getTag().isEmpty()) {
            String tname = rootNode.getTag().trim();
            if(!tname.equals("dummy")) {
                if(tname.equals("include")) {
                    this.parseIncludeAttributes(layoutFile, rootNode);
                } else if(!tname.equals("merge")) {
                    if(tname.equals("fragment")) {
                        AXmlAttribute childClass = rootNode.getAttribute("name");
                        if(childClass == null) {
                            System.err.println("Fragment without class name detected");
                        } else {
                            if(childClass.getType() != 3) {
                                System.err.println("Invalid targer resource " + childClass.getValue() + "for fragment class value");
                            }

                            this.getLayoutClass(childClass.getValue().toString());
                        }
                    } else {
                        SootClass childClass1 = this.getLayoutClass(tname);
                        if(childClass1 != null && (this.isLayoutClass(childClass1) || this.isViewClass(childClass1))) {
                            this.parseLayoutAttributes(layoutFile, childClass1, rootNode);
                        }
                    }
                }
            }

            Iterator childClass2 = rootNode.getChildren().iterator();

            while(childClass2.hasNext()) {
                AXmlNode childNode = (AXmlNode)childClass2.next();
                this.parseLayoutNode(layoutFile, childNode);
            }

        } else {
            System.err.println("Encountered a null or empty node name in file " + layoutFile + ", skipping node...");
        }
    }

    private void parseIncludeAttributes(String layoutFile, AXmlNode rootNode) {
        Iterator var3 = rootNode.getAttributes().entrySet().iterator();

        while(true) {
            AXmlAttribute attr;
            do {
                String attrName;
                do {
                    do {
                        if(!var3.hasNext()) {
                            return;
                        }

                        Entry entry = (Entry)var3.next();
                        attrName = ((String)entry.getKey()).trim();
                        attr = (AXmlAttribute)entry.getValue();
                    } while(!attrName.equals("layout"));
                } while(attr.getType() != 1 && attr.getType() != 17);
            } while(!(attr.getValue() instanceof Integer));

            AbstractResource targetRes = this.resParser.findResource(((Integer)attr.getValue()).intValue());
            if(targetRes == null) {
                System.err.println("Target resource " + attr.getValue() + " for layout include not found");
                return;
            }

            if(!(targetRes instanceof StringResource)) {
                System.err.println("Invalid target node for include tag in layout XML, was " + targetRes.getClass().getName());
                return;
            }

            String targetFile = ((StringResource)targetRes).getValue();
            if(this.callbackMethods.containsKey(targetFile)) {
                Iterator var9 = ((Set)this.callbackMethods.get(targetFile)).iterator();

                while(var9.hasNext()) {
                    String callback = (String)var9.next();
                    this.addCallbackMethod(layoutFile, callback);
                }
            } else {
                this.addToMapSet(this.includeDependencies, targetFile, layoutFile);
            }
        }
    }

    private void parseLayoutAttributes(String layoutFile, SootClass layoutClass, AXmlNode rootNode) {
        boolean isSensitive = false;
        int id = -1;
        HashMap additionalAttributes = this.loadAdditionalAttributes?new HashMap():null;
        Iterator var7 = rootNode.getAttributes().entrySet().iterator();

        while(true) {
            while(true) {
                String attrName;
                AXmlAttribute attr;
                do {
                    do {
                        Entry entry;
                        do {
                            if(!var7.hasNext()) {
                                this.addToMapSet(this.userControls, layoutFile, new LayoutControl(id, layoutClass, isSensitive, additionalAttributes));
                                return;
                            }

                            entry = (Entry)var7.next();
                        } while(entry.getKey() == null);

                        attrName = ((String)entry.getKey()).trim();
                        attr = (AXmlAttribute)entry.getValue();
                    } while(attrName.isEmpty());
                } while(!this.isAndroidNamespace(attr.getNamespace()));

                if(attrName.equals("id") && (attr.getType() == 1 || attr.getType() == 17)) {
                    id = ((Integer)attr.getValue()).intValue();
                } else if(attrName.equals("password")) {
                    if(attr.getType() == 17) {
                        isSensitive = ((Integer)attr.getValue()).intValue() != 0;
                    } else {
                        if(attr.getType() != 18) {
                            throw new RuntimeException("Unknown representation of boolean data type");
                        }

                        isSensitive = ((Boolean)attr.getValue()).booleanValue();
                    }
                } else if(!isSensitive && attrName.equals("inputType") && attr.getType() == 17) {
                    int strData1 = ((Integer)attr.getValue()).intValue();
                    isSensitive = (strData1 & 16) == 16 || (strData1 & 128) == 128 || (strData1 & 144) == 144 || (strData1 & 224) == 224;
                } else if(this.isActionListener(attrName) && attr.getType() == 3 && attr.getValue() instanceof String) {
                    String strData = ((String)attr.getValue()).trim();
                    this.addCallbackMethod(layoutFile, strData);
                } else if(attr.getType() != 3 || !attrName.equals("text")) {
                    if(this.loadAdditionalAttributes) {
                        additionalAttributes.put(attrName, attr.getValue());
                    } else if(attr.getType() == 3) {
                        System.out.println("Found unrecognized XML attribute:  " + attrName);
                    }
                }
            }
        }
    }

    private boolean isActionListener(String name) {
        return name.equals("onClick");
    }

    public Map<Integer, LayoutControl> getUserControlsByID() {
        HashMap res = new HashMap();
        Iterator var2 = this.userControls.values().iterator();

        while(var2.hasNext()) {
            Set controls = (Set)var2.next();
            Iterator var4 = controls.iterator();

            while(var4.hasNext()) {
                LayoutControl lc = (LayoutControl)var4.next();
                res.put(Integer.valueOf(lc.getID()), lc);
            }
        }

        return res;
    }

    public Map<String, Set<LayoutControl>> getUserControls() {
        return this.userControls;
    }

    public Map<String, Set<String>> getCallbackMethods() {
        return this.callbackMethods;
    }

    public void setLoadAdditionalAttributes(boolean loadAdditionalAttributes) {
        this.loadAdditionalAttributes = loadAdditionalAttributes;
    }
}
