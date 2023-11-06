package org.sonar.plugins.cia.sootdemo;

import soot.Body;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.options.Options;

public class SootDemo {
    public static void main(String[] args) {
        Options.v().set_prepend_classpath(true);
        Options.v().set_allow_phantom_refs(true);
        Options.v().set_soot_classpath(
                "plugins/sonar-cia-plugin/build/classes/java/main/org/sonar/plugins/cia/sootdemo");

        Scene scene = Scene.v();
        scene.loadClassAndSupport("FizzBuzz");
        scene.loadNecessaryClasses();
        SootClass sootClass = scene.getSootClass("FizzBuzz");
        SootMethod sootMethod = sootClass.getMethodByName("printFizzBuzz");
        Body body = sootMethod.retrieveActiveBody();

        System.out.println(body);
    }
}
