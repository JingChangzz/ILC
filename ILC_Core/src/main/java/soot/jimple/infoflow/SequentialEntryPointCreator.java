//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package soot.jimple.infoflow;

import soot.Body;
import soot.Local;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.javaToJimple.LocalGenerator;
import soot.jimple.Jimple;
import soot.jimple.infoflow.data.SootMethodAndClass;
import soot.jimple.infoflow.entryPointCreators.BaseEntryPointCreator;
import soot.jimple.infoflow.util.SootMethodRepresentationParser;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SequentialEntryPointCreator extends BaseEntryPointCreator {
    private final Collection<String> methodsToCall = new HashSet<String>();
    private final Collection<String> requiredClasses;

    public SequentialEntryPointCreator(Collection<String> requiredClasses) {
        this.requiredClasses = requiredClasses;
    }

    public Collection<String> getRequiredClasses() {
        HashSet requiredClasses = new HashSet(this.requiredClasses);
        //requiredClasses.addAll(SootMethodRepresentationParser.v().parseClassNames(this.methodsToCall, false).keySet());
        return requiredClasses;
    }

    protected SootMethod createDummyMainInternal(SootMethod mainMethod) {
        for (String m : this.requiredClasses){
            SootClass createdClass = Scene.v().forceResolve(m, 3);
            for (SootMethod s:createdClass.getMethods()){
                this.methodsToCall.add(s.toString());
            }
        }
        Map<String, Set<String>> classMap =
                SootMethodRepresentationParser.v().parseClassNames(methodsToCall, false);

        // create new class:
        Body body = mainMethod.getActiveBody();
        LocalGenerator generator = new LocalGenerator(body);

        // Create the classes
        for (String className : classMap.keySet()) {
            SootClass createdClass = Scene.v().forceResolve(className, SootClass.BODIES);
            createdClass.setApplicationClass();
            Local localVal = generateClassConstructor(createdClass, body);
            if (localVal == null) {
                logger.warn("Cannot generate constructor for class: {}", createdClass);
                continue;
            }

            // Create the method calls
            for (String method : classMap.get(className)) {
                SootMethodAndClass methodAndClass =
                        SootMethodRepresentationParser.v().parseSootMethodString(method);
                SootMethod methodToInvoke = findMethod(Scene.v().getSootClass(
                        methodAndClass.getClassName()), methodAndClass.getSubSignature());

                if (methodToInvoke == null)
                    System.err.println("Method " + methodAndClass + " not found, skipping");
                else if (methodToInvoke.isConcrete() && !methodToInvoke.isConstructor()) {
                    // Load the method
                    methodToInvoke.retrieveActiveBody();
                    buildMethodCall(methodToInvoke, body, localVal, generator);
                }
            }
        }

        // Jimple needs an explicit return statement
        body.getUnits().add(Jimple.v().newReturnVoidStmt());

        return mainMethod;
    }
}
