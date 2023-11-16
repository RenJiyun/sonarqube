package org.sonar.plugins.cia;

import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.scanner.ScannerSide;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import soot.PackManager;
import soot.Scene;
import soot.SootMethod;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.options.Options;
import soot.util.dot.DotGraph;

import java.util.Iterator;
import java.util.List;

@ScannerSide
public class CiaExecutor {
    private static final Logger LOGGER = Loggers.get(CiaExecutor.class);

    public void execute(SensorContext context, Scene scene, List<Endpoint> endpoints) {
        Options.v().setPhaseOption("cg.spark", "on");
        Options.v().setPhaseOption("cg.spark", "on-fly-cg:true");
        Options.v().setPhaseOption("cg.spark", "verbose:true");

        List<SootMethod> entryPoints = endpoints.stream()
                .map(Endpoint::getSootMethod)
                .toList();

        if (entryPoints.isEmpty()) {
            return;
        }

        scene.setEntryPoints(entryPoints);

        LOGGER.info("Soot running packs, entryPoints: {}", entryPoints);
        PackManager.v().runPacks();
        LOGGER.info("Soot running packs done");

        CallGraph cg = scene.getCallGraph();
        printCallGraph(context, cg);
    }

    private void printCallGraph(SensorContext context, CallGraph cg) {
        String projectKey = context.project().key();
        String graphName = projectKey + "-cg-" + System.currentTimeMillis();
        String filename = graphName + ".dot";
        DotGraph dot = new DotGraph(graphName);
        LOGGER.info("Generating call graph to {}", filename);
        for (Iterator<Edge> it = cg.iterator(); it.hasNext(); ) {
            Edge e = it.next();
            String srcMethod = e.getSrc().method().getSignature();
            String tgtMethod = e.getTgt().method().getSignature();

            dot.drawNode(srcMethod);
            dot.drawNode(tgtMethod);
            dot.drawEdge(srcMethod, tgtMethod);
        }
        try {
            dot.plot(filename);
        } catch (Exception e) {
            LOGGER.error("Error while generating call graph", e);
        }
    }
}
