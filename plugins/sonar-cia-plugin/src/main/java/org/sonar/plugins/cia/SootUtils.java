package org.sonar.plugins.cia;

import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import soot.PackManager;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.options.Options;
import soot.util.Chain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author renjiyun
 */
public class SootUtils {

    private static final Logger LOGGER = Loggers.get(SootUtils.class);

    public static void loadClasses(Scene scene, List<String> processDirs, String sootClassPath) {
        Options.v().set_whole_program(true);
        Options.v().set_prepend_classpath(true);
        Options.v().set_allow_phantom_refs(true);
        Options.v().set_exclude(List.of(
                "java.*",
                "javax.*",
                "sun.*",
                "com.sun.*"
        ));
        Options.v().set_no_bodies_for_excluded(true);
        Options.v().set_soot_classpath(sootClassPath);
        Options.v().set_process_dir(processDirs);
        scene.loadNecessaryClasses();
    }

    public static String getQualifiedName(SootClass sootClass) {
        // todo: getPackages() 可能有点问题, 会返回空字符串
        return sootClass.getPackageName() + "." + sootClass.getShortName();
    }

    public static CallGraph getCallGraph(Scene scene, Collection<SootMethod> values) {
        if (values == null || values.isEmpty()) {
            LOGGER.warn("There is no specified entry point, it may take a long time to analyze all methods");
        }
        Options.v().setPhaseOption("cg.spark", "on");
        Options.v().setPhaseOption("cg.spark", "on-fly-cg:true");
        Options.v().setPhaseOption("cg.spark", "verbose:true");
        if (values != null && !values.isEmpty()) {
            scene.setEntryPoints(new ArrayList<>(values));
        }

        PackManager.v().runPacks();
        return scene.getCallGraph();
    }
}
