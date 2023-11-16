package org.sonar.plugins.cia;

import org.sonar.api.batch.fs.FilePredicates;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import soot.Scene;

import java.util.List;

/**
 * @author renjiyun
 */
public class CiaSensor implements Sensor {

    private final CiaConfiguration ciaConfiguration;
    private final CiaExecutor executor;
    private final FileSystem fs;
    private final EndpointRepository endpointRepository;
    private final Scene scene;

    public CiaSensor(CiaConfiguration ciaConfiguration, CiaExecutor executor, FileSystem fs, EndpointRepository endpointRepository) {
        this.ciaConfiguration = ciaConfiguration;
        this.executor = executor;
        this.fs = fs;
        this.endpointRepository = endpointRepository;
        this.scene = Scene.v();
    }

    @Override
    public void describe(SensorDescriptor descriptor) {
        descriptor
                .onlyOnLanguage(CiaConstants.LANGUAGE_KEY)
                .name(this.getClass().getSimpleName());
    }

    @Override
    public void execute(SensorContext context) {
        if (hasFilesToCheck()) {
            List<String> endpointsToBeChecked = ciaConfiguration.getEndpointsToBeChecked(context);
            if (endpointsToBeChecked.isEmpty()) {
                return;
            }

            endpointRepository.init(scene, context);
            List<Endpoint> endpoints = endpointRepository.getEndpointByMatcher(
                    new EndpointRepository.OrMatcher(
                            endpointsToBeChecked.stream()
                                    .map(EndpointRepository.SimpleWildcardMatcher::new)
                                    .toList()
                    )
            );

            if (endpoints != null && !endpoints.isEmpty()) {
                executor.execute(context, scene, endpoints);
            }
        }
    }


    private boolean hasFilesToCheck() {
        FilePredicates predicates = fs.predicates();
        return fs.hasFiles(predicates.and(
                predicates.hasLanguage(CiaConstants.LANGUAGE_KEY),
                predicates.hasType(InputFile.Type.MAIN)));
    }
}
