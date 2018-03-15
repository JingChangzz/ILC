package soot.jimple.infoflow.entryPointCreators;

/**
 * Created by Administrator on 3/14/2018.
 */
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import soot.ArrayType;
import soot.Body;
import soot.BooleanType;
import soot.ByteType;
import soot.CharType;
import soot.DoubleType;
import soot.FloatType;
import soot.G;
import soot.IntType;
import soot.Local;
import soot.LongType;
import soot.RefType;
import soot.Scene;
import soot.ShortType;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.VoidType;
import soot.dava.internal.javaRep.DIntConstant;
import soot.javaToJimple.LocalGenerator;
import soot.jimple.AssignStmt;
import soot.jimple.DoubleConstant;
import soot.jimple.FloatConstant;
import soot.jimple.IfStmt;
import soot.jimple.IntConstant;
import soot.jimple.Jimple;
import soot.jimple.LongConstant;
import soot.jimple.NewArrayExpr;
import soot.jimple.NewExpr;
import soot.jimple.NullConstant;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.StringConstant;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class BaseEntryPointCreator implements IEntryPointCreator {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    protected Map<String, Local> localVarsForClasses = new HashMap();
    private final Set<SootClass> failedClasses = new HashSet();
    private boolean substituteCallParams = false;
    private List<String> substituteClasses;
    private boolean allowSelfReferences = false;
    private final Set<SootMethod> failedMethods = new HashSet();
    protected String dummyClassName = "dummyMainClass";
    protected String dummyMethodName = "dummyMainMethod";

    public BaseEntryPointCreator() {
    }

    public Set<SootClass> getFailedClasses() {
        return new HashSet(this.failedClasses);
    }

    public Set<SootMethod> getFailedMethods() {
        return new HashSet(this.failedMethods);
    }

    public void setSubstituteCallParams(boolean b) {
        this.substituteCallParams = b;
    }

    public void setSubstituteClasses(List<String> l) {
        this.substituteClasses = l;
    }

    public SootMethod createDummyMain() {
        if(this.substituteCallParams) {
            Iterator var1 = this.substituteClasses.iterator();

            while(var1.hasNext()) {
                String className = (String)var1.next();
                Scene.v().forceResolve(className, 3).setApplicationClass();
            }
        }

        return this.createDummyMainInternal();
    }

    public SootMethod createDummyMain(SootMethod dummyMainMethod) {
        if(this.substituteCallParams) {
            Iterator var2 = this.substituteClasses.iterator();

            while(var2.hasNext()) {
                String className = (String)var2.next();
                Scene.v().forceResolve(className, 3).setApplicationClass();
            }
        }

        return this.createDummyMainInternal(dummyMainMethod);
    }

    protected SootMethod createDummyMainInternal() {
        SootMethod emptySootMethod = this.createEmptyMainMethod(Jimple.v().newBody());
        return this.createDummyMainInternal(emptySootMethod);
    }

    protected abstract SootMethod createDummyMainInternal(SootMethod var1);

    protected SootMethod createEmptyMainMethod(Body body) {
        String methodName = this.dummyMethodName;
        SootClass mainClass;
        if(Scene.v().containsClass(this.dummyClassName)) {
            int stringArrayType = 0;

            for(mainClass = Scene.v().getSootClass(this.dummyClassName); mainClass.declaresMethodByName(methodName); methodName = this.dummyMethodName + "_" + stringArrayType++) {
                ;
            }
        } else {
            mainClass = new SootClass(this.dummyClassName);
            Scene.v().addClass(mainClass);
        }

        ArrayType var8 = ArrayType.v(RefType.v("java.lang.String"), 1);
        SootMethod mainMethod = new SootMethod(methodName, Collections.singletonList(var8), VoidType.v());
        body.setMethod(mainMethod);
        mainMethod.setActiveBody(body);
        mainClass.addMethod(mainMethod);
        LocalGenerator lg = new LocalGenerator(body);
        Local paramLocal = lg.generateLocal(var8);
        body.getUnits().addFirst(Jimple.v().newIdentityStmt(paramLocal, Jimple.v().newParameterRef(var8, 0)));
        mainClass.setApplicationClass();
        mainMethod.setModifiers(9);
        return mainMethod;
    }

    protected Stmt buildMethodCall(SootMethod methodToCall, Body body, Local classLocal, LocalGenerator gen) {
        return this.buildMethodCall(methodToCall, body, classLocal, gen, Collections.emptySet());
    }

    protected Stmt buildMethodCall(SootMethod methodToCall, Body body, Local classLocal, LocalGenerator gen, Set<SootClass> parentClasses) {
        assert methodToCall != null : "Current method was null";

        assert body != null : "Body was null";

        assert gen != null : "Local generator was null";

        if(classLocal == null && !methodToCall.isStatic()) {
            this.logger.warn("Cannot call method {}, because there is no local for base object: {}", methodToCall, methodToCall.getDeclaringClass());
            this.failedMethods.add(methodToCall);
            return null;
        } else {
            LinkedList args = new LinkedList();
            Object invokeExpr;
            if(methodToCall.getParameterCount() > 0) {
                Type returnLocal;
                HashSet val;
                for(Iterator stmt = methodToCall.getParameterTypes().iterator(); stmt.hasNext(); args.add(this.getValueForType(body, gen, returnLocal, val, parentClasses))) {
                    returnLocal = (Type)stmt.next();
                    val = new HashSet();
                    if(!this.allowSelfReferences) {
                        val.add(methodToCall.getDeclaringClass());
                    }
                }

                if(methodToCall.isStatic()) {
                    invokeExpr = Jimple.v().newStaticInvokeExpr(methodToCall.makeRef(), args);
                } else {
                    assert classLocal != null : "Class local method was null for non-static method call";

                    if(methodToCall.isConstructor()) {
                        invokeExpr = Jimple.v().newSpecialInvokeExpr(classLocal, methodToCall.makeRef(), args);
                    } else {
                        invokeExpr = Jimple.v().newVirtualInvokeExpr(classLocal, methodToCall.makeRef(), args);
                    }
                }
            } else if(methodToCall.isStatic()) {
                invokeExpr = Jimple.v().newStaticInvokeExpr(methodToCall.makeRef());
            } else {
                assert classLocal != null : "Class local method was null for non-static method call";

                if(methodToCall.isConstructor()) {
                    invokeExpr = Jimple.v().newSpecialInvokeExpr(classLocal, methodToCall.makeRef());
                } else {
                    invokeExpr = Jimple.v().newVirtualInvokeExpr(classLocal, methodToCall.makeRef());
                }
            }

            Object stmt1;
            if(!(methodToCall.getReturnType() instanceof VoidType)) {
                Local returnLocal1 = gen.generateLocal(methodToCall.getReturnType());
                stmt1 = Jimple.v().newAssignStmt(returnLocal1, (Value)invokeExpr);
            } else {
                stmt1 = Jimple.v().newInvokeStmt((Value)invokeExpr);
            }

            body.getUnits().add((Unit)stmt1);
            Iterator returnLocal2 = args.iterator();

            while(returnLocal2.hasNext()) {
                Object val1 = returnLocal2.next();
                if(val1 instanceof Local && ((Value)val1).getType() instanceof RefType) {
                    body.getUnits().add(Jimple.v().newAssignStmt((Value)val1, NullConstant.v()));
                }
            }

            return (Stmt)stmt1;
        }
    }

    private Value getValueForType(Body body, LocalGenerator gen, Type tp, Set<SootClass> constructionStack, Set<SootClass> parentClasses) {
        if(isSimpleType(tp.toString())) {
            return this.getSimpleDefaultValue(tp.toString());
        } else if(tp instanceof RefType) {
            SootClass arrVal1 = ((RefType)tp).getSootClass();
            if(arrVal1 != null) {
                Iterator val = parentClasses.iterator();

                while(val.hasNext()) {
                    SootClass parent = (SootClass)val.next();
                    if(this.isCompatible(parent, arrVal1)) {
                        Value val1 = (Value)this.localVarsForClasses.get(parent.getName());
                        if(val1 != null) {
                            return val1;
                        }
                    }
                }

                Local val2 = this.generateClassConstructor(arrVal1, body, constructionStack, parentClasses);
                if(val2 == null) {
                    return NullConstant.v();
                } else {
                    return val2;
                }
            } else {
                throw new RuntimeException("Should never see me");
            }
        } else if(tp instanceof ArrayType) {
            Value arrVal = this.buildArrayOfType(body, gen, (ArrayType)tp, constructionStack, parentClasses);
            if(arrVal == null) {
                this.logger.warn("Array parameter substituted by null");
                return NullConstant.v();
            } else {
                return arrVal;
            }
        } else {
            this.logger.warn("Unsupported parameter type: {}", tp.toString());
            return null;
        }
    }

    private Value buildArrayOfType(Body body, LocalGenerator gen, ArrayType tp, Set<SootClass> constructionStack, Set<SootClass> parentClasses) {
        Local local = gen.generateLocal(tp);
        NewArrayExpr newArrayExpr = Jimple.v().newNewArrayExpr(tp.getElementType(), IntConstant.v(1));
        AssignStmt assignArray = Jimple.v().newAssignStmt(local, newArrayExpr);
        body.getUnits().add(assignArray);
        AssignStmt assign = Jimple.v().newAssignStmt(Jimple.v().newArrayRef(local, IntConstant.v(0)), this.getValueForType(body, gen, tp.getElementType(), constructionStack, parentClasses));
        body.getUnits().add(assign);
        return local;
    }

    protected Local generateClassConstructor(SootClass createdClass, Body body) {
        return this.generateClassConstructor(createdClass, body, new HashSet(), Collections.emptySet());
    }

    protected Local generateClassConstructor(SootClass createdClass, Body body, Set<SootClass> parentClasses) {
        return this.generateClassConstructor(createdClass, body, new HashSet(), parentClasses);
    }

    private Local generateClassConstructor(SootClass createdClass, Body body, Set<SootClass> constructionStack, Set<SootClass> parentClasses) {
        if(createdClass != null && !this.failedClasses.contains(createdClass)) {
            if(!createdClass.isPhantom() && !createdClass.isPhantomClass()) {
                LocalGenerator generator = new LocalGenerator(body);
                if(isSimpleType(createdClass.toString())) {
                    Local isInnerClass1 = generator.generateLocal(this.getSimpleTypeFromType(createdClass.getType()));
                    AssignStmt outerClass1 = Jimple.v().newAssignStmt(isInnerClass1, this.getSimpleDefaultValue(createdClass.toString()));
                    body.getUnits().add(outerClass1);
                    return isInnerClass1;
                } else {
                    boolean isInnerClass = createdClass.getName().contains("$");
                    String outerClass = isInnerClass?createdClass.getName().substring(0, createdClass.getName().lastIndexOf("$")):"";
                    if(!constructionStack.add(createdClass)) {
                        this.logger.warn("Ran into a constructor generation loop for class " + createdClass + ", substituting with null...");
                        Local classes2 = generator.generateLocal(RefType.v(createdClass));
                        AssignStmt currentMethod2 = Jimple.v().newAssignStmt(classes2, NullConstant.v());
                        body.getUnits().add(currentMethod2);
                        return classes2;
                    } else if(!createdClass.isInterface() && !createdClass.isAbstract()) {
                        Iterator classes1 = createdClass.getMethods().iterator();

                        SootMethod currentMethod1;
                        do {
                            if(!classes1.hasNext()) {
                                this.logger.warn("Could not find a suitable constructor for class {}", createdClass.getName());
                                this.failedClasses.add(createdClass);
                                return null;
                            }

                            currentMethod1 = (SootMethod)classes1.next();
                        } while(currentMethod1.isPrivate() || !currentMethod1.isConstructor());

                        LinkedList params1 = new LinkedList();
                        Iterator newExpr1 = currentMethod1.getParameterTypes().iterator();

                        while(true) {
                            while(newExpr1.hasNext()) {
                                Type tempLocal = (Type)newExpr1.next();
                                String assignStmt = tempLocal.toString().replaceAll("\\[\\]]", "");
                                if(tempLocal instanceof RefType && isInnerClass && assignStmt.equals(outerClass) && this.localVarsForClasses.containsKey(assignStmt)) {
                                    params1.add(this.localVarsForClasses.get(assignStmt));
                                } else {
                                    params1.add(this.getValueForType(body, generator, tempLocal, constructionStack, parentClasses));
                                }
                            }

                            NewExpr newExpr2 = Jimple.v().newNewExpr(RefType.v(createdClass));
                            Local tempLocal1 = generator.generateLocal(RefType.v(createdClass));
                            AssignStmt assignStmt1 = Jimple.v().newAssignStmt(tempLocal1, newExpr2);
                            body.getUnits().add(assignStmt1);
                            SpecialInvokeExpr vInvokeExpr;
                            if(!params1.isEmpty() && !params1.contains((Object)null)) {
                                vInvokeExpr = Jimple.v().newSpecialInvokeExpr(tempLocal1, currentMethod1.makeRef(), params1);
                            } else {
                                vInvokeExpr = Jimple.v().newSpecialInvokeExpr(tempLocal1, currentMethod1.makeRef());
                            }

                            if(!(currentMethod1.getReturnType() instanceof VoidType)) {
                                Local possibleReturn = generator.generateLocal(currentMethod1.getReturnType());
                                AssignStmt assignStmt2 = Jimple.v().newAssignStmt(possibleReturn, vInvokeExpr);
                                body.getUnits().add(assignStmt2);
                            } else {
                                body.getUnits().add(Jimple.v().newInvokeStmt(vInvokeExpr));
                            }

                            return tempLocal1;
                        }
                    } else if(this.substituteCallParams) {
                        List classes;
                        if(createdClass.isInterface()) {
                            classes = Scene.v().getActiveHierarchy().getImplementersOf(createdClass);
                        } else {
                            classes = Scene.v().getActiveHierarchy().getSubclassesOf(createdClass);
                        }

                        Iterator currentMethod = classes.iterator();

                        while(currentMethod.hasNext()) {
                            SootClass params = (SootClass)currentMethod.next();
                            if(this.substituteClasses.contains(params.toString())) {
                                Local newExpr = this.generateClassConstructor(params, body, constructionStack, parentClasses);
                                if(newExpr != null) {
                                    return newExpr;
                                }
                            }
                        }

                        this.logger.warn("Cannot create valid constructor for {}, because it is {} and cannot substitute with subclass", createdClass, createdClass.isInterface()?"an interface":(createdClass.isAbstract()?"abstract":""));
                        this.failedClasses.add(createdClass);
                        return null;
                    } else {
                        this.logger.warn("Cannot create valid constructor for {}, because it is {} and cannot substitute with subclass", createdClass, createdClass.isInterface()?"an interface":(createdClass.isAbstract()?"abstract":""));
                        this.failedClasses.add(createdClass);
                        return null;
                    }
                }
            } else {
                this.failedClasses.add(createdClass);
                return null;
            }
        } else {
            return null;
        }
    }

    private Type getSimpleTypeFromType(Type type) {
        if(type.toString().equals("java.lang.String")) {
            assert type instanceof RefType;

            return RefType.v(((RefType)type).getSootClass());
        } else if(type.toString().equals("void")) {
            return VoidType.v();
        } else if(type.toString().equals("char")) {
            return CharType.v();
        } else if(type.toString().equals("byte")) {
            return ByteType.v();
        } else if(type.toString().equals("short")) {
            return ShortType.v();
        } else if(type.toString().equals("int")) {
            return IntType.v();
        } else if(type.toString().equals("float")) {
            return FloatType.v();
        } else if(type.toString().equals("long")) {
            return LongType.v();
        } else if(type.toString().equals("double")) {
            return DoubleType.v();
        } else if(type.toString().equals("boolean")) {
            return BooleanType.v();
        } else {
            throw new RuntimeException("Unknown simple type: " + type);
        }
    }

    protected static boolean isSimpleType(String t) {
        return t.equals("java.lang.String") || t.equals("void") || t.equals("char") || t.equals("byte") || t.equals("short") || t.equals("int") || t.equals("float") || t.equals("long") || t.equals("double") || t.equals("boolean");
    }

    protected Value getSimpleDefaultValue(String t) {
        return (Value)(t.equals("java.lang.String")?StringConstant.v(""):(t.equals("char")?DIntConstant.v(0, CharType.v()):(t.equals("byte")?DIntConstant.v(0, ByteType.v()):(t.equals("short")?DIntConstant.v(0, ShortType.v()):(t.equals("int")?IntConstant.v(0):(t.equals("float")?FloatConstant.v(0.0F):(t.equals("long")?LongConstant.v(0L):(t.equals("double")?DoubleConstant.v(0.0D):(t.equals("boolean")?DIntConstant.v(0, BooleanType.v()):G.v().soot_jimple_NullConstant())))))))));
    }

    protected SootMethod findMethod(SootClass currentClass, String subsignature) {
        return currentClass.declaresMethod(subsignature)?currentClass.getMethod(subsignature):(currentClass.hasSuperclass()?this.findMethod(currentClass.getSuperclass(), subsignature):null);
    }

    protected boolean isCompatible(SootClass actual, SootClass expected) {
        for(SootClass act = actual; !act.getName().equals(expected.getName()); act = act.getSuperclass()) {
            if(expected.isInterface()) {
                Iterator var4 = act.getInterfaces().iterator();

                while(var4.hasNext()) {
                    SootClass intf = (SootClass)var4.next();
                    if(intf.getName().equals(expected.getName())) {
                        return true;
                    }
                }
            }

            if(!act.hasSuperclass()) {
                return false;
            }
        }

        return true;
    }

    protected void eliminateSelfLoops(Body body) {
        Iterator unitIt = body.getUnits().iterator();

        while(unitIt.hasNext()) {
            Unit u = (Unit)unitIt.next();
            if(u instanceof IfStmt) {
                IfStmt ifStmt = (IfStmt)u;
                if(ifStmt.getTarget() == ifStmt) {
                    unitIt.remove();
                }
            }
        }

    }

    public void setDummyClassName(String dummyClassName) {
        this.dummyClassName = dummyClassName;
    }

    public void setDummyMethodName(String dummyMethodName) {
        this.dummyMethodName = dummyMethodName;
    }

    public void setAllowSelfReferences(boolean value) {
        this.allowSelfReferences = value;
    }
}
