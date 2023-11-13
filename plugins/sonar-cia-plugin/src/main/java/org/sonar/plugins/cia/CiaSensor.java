package org.sonar.plugins.cia;

import org.sonar.api.batch.fs.FilePredicates;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import soot.Scene;
import soot.SootMethod;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author renjiyun
 */
public class CiaSensor implements Sensor {
    private final CiaExecutor executor;
    private final FileSystem fs;

    public CiaSensor(CiaExecutor executor, FileSystem fs) {
        this.executor = executor;
        this.fs = fs;
    }

    @Override
    public void describe(SensorDescriptor descriptor) {
        descriptor.onlyOnLanguage(CiaConstants.LANGUAGE_KEY)
                .name("CiaSensor");
    }

    @Override
    public void execute(SensorContext context) {
        List<String> endpointsToBeChecked = getEndpointsToBeChecked(context);
        if (hasFilesToCheck() && !endpointsToBeChecked.isEmpty()) {
            // 加载待处理目录下的所有类
            executor.init(Scene.v(), getProcessDirs(context), getSootClassPath(context));
            Map<String, SootMethod> realEndpointsToBeChecked = executor.parseEndpoints(endpointsToBeChecked);
            executor.execute(realEndpointsToBeChecked);
        }
    }

    /**
     * 获取目标类所在的类路径
     *
     * @param context
     * @return
     */
    private String getSootClassPath(SensorContext context) {
        return null;
    }

    /**
     * 获取需要处理的目录
     *
     * @param context
     * @return
     */
    private List<String> getProcessDirs(SensorContext context) {
        return null;
    }


    /**
     * 获取需要检查的端点
     *
     * @param context
     * @return
     */
    private List<String> getEndpointsToBeChecked(SensorContext context) {
        String[] endPointsToBeChecked = context.config().getStringArray(CiaConstants.ENDPOINTS_TO_BE_CHECKED);
        if (endPointsToBeChecked == null || endPointsToBeChecked.length == 0) {
            return new ArrayList<>();
        }
        return List.of(endPointsToBeChecked);
    }

    private boolean hasFilesToCheck() {
        FilePredicates predicates = fs.predicates();
        return fs.hasFiles(predicates.and(
                predicates.hasLanguage(CiaConstants.LANGUAGE_KEY),
                predicates.hasType(InputFile.Type.MAIN)));
    }
}
