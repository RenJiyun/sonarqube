package org.sonar.plugins.cia.sootdemo;

import soot.*;
import soot.options.Options;
import soot.toolkits.graph.BriefUnitGraph;
import soot.util.dot.DotGraph;

/**
 * @author renjiyun
 */
public class UnitGraphDemo {
    public static void main(String[] args) {
        Options.v().set_prepend_classpath(true);
        Options.v().set_allow_phantom_refs(true);
        Options.v().set_soot_classpath(
                "plugins/sonar-cia-plugin/build/classes/java/main/org/sonar/plugins/cia/sootdemo");

        String className = "Fizz";
        String methodName = "fizz";
        Scene scene = Scene.v();
        scene.loadClassAndSupport(className);
        scene.loadNecessaryClasses();
        SootClass sootClass = scene.getSootClass(className);
        SootMethod sootMethod = sootClass.getMethodByName(methodName);
        briefUnitGraph(sootMethod);
    }

    /**
     * draw the BriefUnitGraph of the method
     *
     * @param method
     */
    private static void briefUnitGraph(SootMethod method) {
        String fileName = method.getName() + "-CFG-BUG";
        DotGraph dot = new DotGraph(fileName);
        BriefUnitGraph cfg = new BriefUnitGraph(method.retrieveActiveBody());
        for (Unit unit : cfg) {
            String unitHash = String.valueOf(unit.toString().hashCode());
            dot.drawNode(unitHash).setLabel(unit.toString());

            for (Unit succUnit : cfg.getSuccsOf(unit)) {
                String succUnitHash = String.valueOf(succUnit.toString().hashCode());
                dot.drawEdge(unitHash, succUnitHash);
            }
        }
        dot.plot("plugins/sonar-cia-plugin/build/tmp/" + fileName + ".dot");
    }
}
