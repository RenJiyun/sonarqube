package org.sonar.plugins.cia;

import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.scanner.ScannerSide;

import java.util.ArrayList;
import java.util.List;

@ScannerSide
public class CiaConfiguration {
    private static final String ENDPOINTS_TO_BE_CHECKED = "sonar.cia.endpoints.check";

    private static final String SOOT_CLASS_PATH = "sonar.cia.soot.classpath";
    private static final String PROCESS_DIRS = "sonar.cia.process.dirs";

    public String getSootClassPath(SensorContext context) {
        return context.config().get(SOOT_CLASS_PATH).orElse("");
    }

    public List<String> getProcessDirs(SensorContext context) {
        return context.config().getStringArray(PROCESS_DIRS) == null
                ? new ArrayList<>()
                : List.of(context.config().getStringArray(PROCESS_DIRS));
    }

    public List<String> getEndpointsToBeChecked(SensorContext context) {
        String[] endPointsToBeChecked = context.config().getStringArray(ENDPOINTS_TO_BE_CHECKED);
        if (endPointsToBeChecked == null || endPointsToBeChecked.length == 0) {
            return new ArrayList<>();
        }
        return List.of(endPointsToBeChecked);
    }
}
