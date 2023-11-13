package org.sonar.plugins.cia.sootdemo;

import soot.PackManager;
import soot.Scene;
import soot.SootClass;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.options.Options;
import soot.util.dot.DotGraph;

import java.util.Iterator;
import java.util.List;

public class CallGraphDemo {
    public static void main(String[] args) {
        Options.v().set_whole_program(true);
        Options.v().set_prepend_classpath(true);
        Options.v().set_allow_phantom_refs(true);
        Options.v().set_exclude(getJavaPackages());
        Options.v().set_no_bodies_for_excluded(true);

        Options.v().setPhaseOption("cg.spark", "on");
        Options.v().setPhaseOption("cg.spark", "on-fly-cg:true");
        Options.v().setPhaseOption("cg.spark", "verbose:true");

        Options.v().set_soot_classpath(
                "plugins/sonar-cia-plugin/build/classes/java/main"
        );
        Options.v().set_process_dir(List.of(
                "plugins/sonar-cia-plugin/build/classes/java/main/org/sonar/plugins/cia/cg"
        ));

        Scene scene = Scene.v();
        scene.loadNecessaryClasses();
        Scene.v().setEntryPoints(List.of(
                scene.getSootClass("FizzBuzz").getMethodByName("fizzBuzz")
        ));

        PackManager.v().runPacks();
        CallGraph cg = scene.getCallGraph();

        printCallGraph(cg, "Fizz", "fizz");
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
