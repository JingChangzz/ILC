//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package soot.jimple.infoflow.android.source.parsers.xml;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import soot.jimple.infoflow.android.data.AndroidMethod;
import soot.jimple.infoflow.android.data.AndroidMethod.CATEGORY;
import soot.jimple.infoflow.data.SootMethodAndClass;
import soot.jimple.infoflow.source.data.AccessPathTuple;
import soot.jimple.infoflow.source.data.ISourceSinkDefinitionProvider;
import soot.jimple.infoflow.source.data.SourceSinkDefinition;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class XMLSourceSinkParser extends DefaultHandler implements ISourceSinkDefinitionProvider {
    private String methodSignature;
    private String methodCategory;
    private boolean isSource;
    private boolean isSink;
    private String[] pathElements;
    private String[] pathElementTypes;
    private int paramIndex;
    private List<String> paramTypes = new ArrayList();
    private String accessPathParentElement = "";
    private Set<AccessPathTuple> baseAPs = new HashSet();
    private List<Set<AccessPathTuple>> paramAPs = new ArrayList();
    private Set<AccessPathTuple> returnAPs = new HashSet();
    private Map<SootMethodAndClass, SourceSinkDefinition> sourcesAndSinks = new HashMap();
    private Set<SourceSinkDefinition> sources = new HashSet();
    private Set<SourceSinkDefinition> sinks = new HashSet();
    private static final String XSD_FILE_PATH = "exchangeFormat.xsd";
    private static final String W3C_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";

    public static XMLSourceSinkParser fromFile(String fileName) throws IOException {
        if(!verifyXML(fileName)) {
            throw new RuntimeException("The XML-File isn\'t valid");
        } else {
            XMLSourceSinkParser pmp = new XMLSourceSinkParser(fileName);
            return pmp;
        }
    }

    public Set<SourceSinkDefinition> getSources() {
        return this.sources;
    }

    public Set<SourceSinkDefinition> getSinks() {
        return this.sinks;
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        String qNameLower = qName.toLowerCase();
        byte var7 = -1;
        switch(qNameLower.hashCode()) {
            case -1287538889:
                if(qNameLower.equals("pathelement")) {
                    var7 = 5;
                }
                break;
            case -1141192823:
                if(qNameLower.equals("accesspath")) {
                    var7 = 1;
                }
                break;
            case -1077554975:
                if(qNameLower.equals("method")) {
                    var7 = 0;
                }
                break;
            case -934396624:
                if(qNameLower.equals("return")) {
                    var7 = 3;
                }
                break;
            case 3016401:
                if(qNameLower.equals("base")) {
                    var7 = 2;
                }
                break;
            case 106436749:
                if(qNameLower.equals("param")) {
                    var7 = 4;
                }
        }

        String pathElementIdx2;
        switch(var7) {
            case 0:
                if(attributes != null) {
                    this.methodSignature = attributes.getValue("signature").trim();
                    this.methodCategory = attributes.getValue("category").trim();
                }
                break;
            case 1:
                if(attributes != null) {
                    pathElementIdx2 = attributes.getValue("isSource");
                    if(pathElementIdx2 != null && !pathElementIdx2.isEmpty()) {
                        this.isSource = pathElementIdx2.equalsIgnoreCase("true");
                    }

                    pathElementIdx2 = attributes.getValue("isSink");
                    if(pathElementIdx2 != null && !pathElementIdx2.isEmpty()) {
                        this.isSink = pathElementIdx2.equalsIgnoreCase("true");
                    }

                    pathElementIdx2 = attributes.getValue("length");
                    if(pathElementIdx2 != null && !pathElementIdx2.isEmpty()) {
                        this.pathElements = new String[Integer.parseInt(pathElementIdx2)];
                        this.pathElementTypes = new String[Integer.parseInt(pathElementIdx2)];
                    }
                }
                break;
            case 2:
                this.accessPathParentElement = qNameLower;
                break;
            case 3:
                this.accessPathParentElement = qNameLower;
                break;
            case 4:
                if(this.methodSignature != null && attributes != null) {
                    pathElementIdx2 = attributes.getValue("index");
                    if(pathElementIdx2 != null && !pathElementIdx2.isEmpty()) {
                        this.paramIndex = Integer.parseInt(pathElementIdx2);
                    }

                    pathElementIdx2 = attributes.getValue("type");
                    if(pathElementIdx2 != null && !pathElementIdx2.isEmpty()) {
                        this.paramTypes.add(pathElementIdx2.trim());
                    }
                }

                this.accessPathParentElement = qNameLower;
                break;
            case 5:
                if(this.methodSignature != null && attributes != null) {
                    boolean pathElementIdx = true;
                    String tempStr = attributes.getValue("index");
                    if(tempStr != null && !tempStr.isEmpty()) {
                        int pathElementIdx1 = Integer.parseInt(tempStr.trim());
                        tempStr = attributes.getValue("field");
                        if(tempStr != null && !tempStr.isEmpty()) {
                            if(pathElementIdx1 >= this.pathElements.length) {
                                throw new RuntimeException("Path element index out of range");
                            }

                            this.pathElements[pathElementIdx1] = tempStr;
                        }

                        tempStr = attributes.getValue("type");
                        if(tempStr != null && !tempStr.isEmpty()) {
                            if(pathElementIdx1 >= this.pathElementTypes.length) {
                                throw new RuntimeException("Path element type index out of range");
                            }

                            this.pathElementTypes[pathElementIdx1] = tempStr;
                        }
                    }
                }
        }

    }

    public void characters(char[] ch, int start, int length) throws SAXException {
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        String qNameLower = qName.toLowerCase();
        byte var6 = -1;
        switch(qNameLower.hashCode()) {
            case -1287538889:
                if(qNameLower.equals("pathelement")) {
                    var6 = 5;
                }
                break;
            case -1141192823:
                if(qNameLower.equals("accesspath")) {
                    var6 = 1;
                }
                break;
            case -1077554975:
                if(qNameLower.equals("method")) {
                    var6 = 0;
                }
                break;
            case -934396624:
                if(qNameLower.equals("return")) {
                    var6 = 3;
                }
                break;
            case 3016401:
                if(qNameLower.equals("base")) {
                    var6 = 2;
                }
                break;
            case 106436749:
                if(qNameLower.equals("param")) {
                    var6 = 4;
                }
        }

        switch(var6) {
            case 0:
                if(this.methodSignature != null) {
                    AndroidMethod tempMeth = AndroidMethod.createFromSignature(this.methodSignature);
                    if(this.methodCategory != null) {
                        String ssd = this.methodCategory.toUpperCase().trim();
                        tempMeth.setCategory(CATEGORY.valueOf(ssd));
                    }

                    tempMeth.setSink(this.isSink);
                    tempMeth.setSource(this.isSource);
                    SourceSinkDefinition ssd1 = new SourceSinkDefinition(tempMeth, this.baseAPs, (Set[])this.paramAPs.toArray(new Set[this.paramAPs.size()]), this.returnAPs);
                    if(this.sourcesAndSinks.containsKey(tempMeth)) {
                        ((SourceSinkDefinition)this.sourcesAndSinks.get(tempMeth)).merge(ssd1);
                    } else {
                        this.sourcesAndSinks.put(tempMeth, ssd1);
                    }

                    this.methodSignature = null;
                    this.methodCategory = null;
                    this.baseAPs = new HashSet();
                    this.paramAPs = new ArrayList();
                    this.returnAPs = new HashSet();
                }
                break;
            case 1:
                if(this.isSource || this.isSink) {
                    if(this.pathElements != null && this.pathElements.length == 0 && this.pathElementTypes != null && this.pathElementTypes.length == 0) {
                        this.pathElements = null;
                        this.pathElementTypes = null;
                    }

                    AccessPathTuple apt = AccessPathTuple.fromPathElements(this.pathElements, this.pathElementTypes, this.isSource, this.isSink);
                    String var10 = this.accessPathParentElement;
                    byte var11 = -1;
                    switch(var10.hashCode()) {
                        case -934396624:
                            if(var10.equals("return")) {
                                var11 = 1;
                            }
                            break;
                        case 3016401:
                            if(var10.equals("base")) {
                                var11 = 0;
                            }
                            break;
                        case 106436749:
                            if(var10.equals("param")) {
                                var11 = 2;
                            }
                    }

                    switch(var11) {
                        case 0:
                            this.baseAPs.add(apt);
                            break;
                        case 1:
                            this.returnAPs.add(apt);
                            break;
                        case 2:
                            while(this.paramAPs.size() <= this.paramIndex) {
                                this.paramAPs.add(new HashSet());
                            }

                            ((Set)this.paramAPs.get(this.paramIndex)).add(apt);
                    }
                }

                this.isSource = false;
                this.isSink = false;
                this.pathElements = null;
                this.pathElementTypes = null;
                break;
            case 2:
                this.accessPathParentElement = "";
                break;
            case 3:
                this.accessPathParentElement = "";
                break;
            case 4:
                this.accessPathParentElement = "";
                this.paramIndex = -1;
                this.paramTypes.clear();
            case 5:
        }

    }

    private XMLSourceSinkParser(String fileName) {
        SAXParserFactory pf = SAXParserFactory.newInstance();

        try {
            SAXParser e = pf.newSAXParser();
            e.parse(fileName, this);
        } catch (ParserConfigurationException var7) {
            var7.printStackTrace();
        } catch (SAXException var8) {
            var8.printStackTrace();
        } catch (IOException var9) {
            var9.printStackTrace();
        }

        Iterator e1 = this.sourcesAndSinks.values().iterator();

        while(e1.hasNext()) {
            SourceSinkDefinition def = (SourceSinkDefinition)e1.next();
            SourceSinkDefinition sourceDef = def.getSourceOnlyDefinition();
            if(!sourceDef.isEmpty()) {
                this.sources.add(sourceDef);
            }

            SourceSinkDefinition sinkDef = def.getSinkOnlyDefinition();
            if(!sinkDef.isEmpty()) {
                this.sinks.add(sinkDef);
            }
        }

    }

    private static boolean verifyXML(String fileName) {
        SchemaFactory sf = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
        StreamSource xsdFile = new StreamSource(new File("exchangeFormat.xsd"));
        StreamSource xmlFile = new StreamSource(new File(fileName));
        boolean validXML = false;

        try {
            Schema e = sf.newSchema(xsdFile);
            Validator validator = e.newValidator();

            try {
                validator.validate(xmlFile);
                validXML = true;
            } catch (IOException var8) {
                var8.printStackTrace();
            }

            if(!validXML) {
                new IOException("File isn\'t  valid against the xsd");
            }
        } catch (SAXException var9) {
            var9.printStackTrace();
        }

        return validXML;
    }

    public Set<SourceSinkDefinition> getAllMethods() {
        HashSet sourcesSinks = new HashSet(this.sources.size() + this.sinks.size());
        sourcesSinks.addAll(this.sources);
        sourcesSinks.addAll(this.sinks);
        return sourcesSinks;
    }
}
