package org.sonar.plugins.cia.sootdemo;

import soot.PackManager;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.options.Options;
import soot.util.dot.DotGraph;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CallGraphDemo {
    public static void main(String[] args) {
        Options.v().set_whole_program(true);
        Options.v().setPhaseOption("cg.spark", "on");
        Options.v().setPhaseOption("cg.spark", "on-fly-cg:false");
        Options.v().set_prepend_classpath(true);
        Options.v().set_allow_phantom_refs(true);
        Options.v().set_soot_classpath(
                "plugins/sonar-cia-plugin/build/classes/java/main/org/sonar/plugins/cia/sootdemo");

        Options.v().set_process_dir(
                List.of("plugins/sonar-cia-plugin/build/classes/java/main/org/sonar/plugins/cia/sootdemo"));
        Options.v().set_exclude(getJavaPackages());
        Options.v().set_no_bodies_for_excluded(true);

        String className = "Fizz";
        String methodName = "fizz";
        Scene scene = Scene.v();
        scene.loadClassAndSupport(className);
        scene.loadNecessaryClasses();
        SootClass sootClass = scene.getSootClass(className);
        SootMethod specificMethod = sootClass.getMethodByName(methodName);
        sootClass.setApplicationClass();

        scene.getSootClass("Buzz").setApplicationClass();

        List<SootMethod> entryPoints = new ArrayList<>();
        entryPoints.add(specificMethod);
        Scene.v().setEntryPoints(entryPoints);

        PackManager.v().runPacks();
        CallGraph cg = scene.getCallGraph();

        printCallGraph(cg, className, methodName);
    }

    private static void printCallGraph(CallGraph cg, String className, String methodName) {
        String graphName = className + "-" + methodName + "-cg";
        String filename = graphName + ".dot";
        DotGraph dot = new DotGraph(graphName);
        for (Iterator<Edge> it = cg.iterator(); it.hasNext(); ) {
            Edge e = it.next();
            String srcMethod = e.getSrc().method().getSignature();
            String tgtMethod = e.getTgt().method().getSignature();

            dot.drawNode(srcMethod);
            dot.drawNode(tgtMethod);
            dot.drawEdge(srcMethod, tgtMethod);
        }
        try {
            dot.plot("plugins/sonar-cia-plugin/build/tmp/" + filename);
        } catch (Exception e) {
            System.err.println("Error in writing to dot file: " + e.getMessage());
        }
    }

    private static List<String> getJavaPackages() {
        return List.of(new String[]{
                "java.*",
                "javax.*",
                "sun.*",
                "com.sun.*"
        });
    }
}
