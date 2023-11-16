package org.sonar.plugins.cia;

import org.sonar.api.scanner.ScannerSide;
import soot.PackManager;
import soot.Scene;
import soot.SootMethod;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.options.Options;

import java.util.List;

@ScannerSide
public class CiaExecutor {
    public void execute(Scene scene, List<Endpoint> endpoints) {
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
        PackManager.v().runPacks();

        CallGraph cg = scene.getCallGraph();
        System.out.println(cg.size());
    }
}
