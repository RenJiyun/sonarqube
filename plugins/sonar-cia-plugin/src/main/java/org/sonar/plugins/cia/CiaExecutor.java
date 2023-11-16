package org.sonar.plugins.cia;

import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.scanner.ScannerSide;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import soot.*;
import soot.jimple.spark.SparkTransformer;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.CallGraphPack;
import soot.jimple.toolkits.callgraph.Edge;
import soot.options.Options;
import soot.util.dot.DotGraph;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

@ScannerSide
public class CiaExecutor {
    private static final Logger LOGGER = Loggers.get(CiaExecutor.class);

    private PackManager packManager;

    /**
     * 用于裁剪 cg 的 Transformer
     */
    private final SceneTransformer cgEdgePruner = new SceneTransformer() {
        @Override
        protected void internalTransform(String phaseName, Map<String, String> options) {
            CallGraph cg = Scene.v().getCallGraph();
            LOGGER.info("Pruning call graph");
            for (Iterator<Edge> it = cg.iterator(); it.hasNext(); ) {
                Edge e = it.next();
                if (needPrune(e)) {
                    LOGGER.info("Pruning edge: {}", e);
                    it.remove();
                }
            }
        }

        private boolean needPrune(Edge e) {
            return !e.getTgt().method().getName().contains("wlzq");
        }
    };


    public CiaExecutor() {
        this.packManager = PackManager.v();
        ScenePack wjtpPack = (ScenePack) packManager.getPack("wjtp");
        wjtpPack.insertBefore(new Transform("wjtp.cia", cgEdgePruner), "wjtp.mhp");
    }

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
        packManager.runPacks();
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
