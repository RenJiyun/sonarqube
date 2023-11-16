package org.sonar.plugins.cia;

import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.scanner.ScannerSide;
import soot.Scene;
import soot.options.Options;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@ScannerSide
public class EndpointRepository {
    private static final String ENDPOINT_SUPER_CLASS = "com.wlzq.core.BaseService";
    private static final String ENDPOINT_RETURN_TYPE = "com.wlzq.core.dto.ResultDto";


    private final CiaConfiguration ciaConfiguration;

    private List<EndpointClass> endpointClasses;
    private Map<String, EndpointClass> endpointClassMap;

    private List<Endpoint> endpoints;
    private Map<String, Endpoint> endpointMap;

    public EndpointRepository(CiaConfiguration ciaConfiguration) {
        this.ciaConfiguration = ciaConfiguration;
    }

    public void init(Scene scene, SensorContext context) {
        List<String> processDirs = ciaConfiguration.getProcessDirs(context);
        String sootClassPath = ciaConfiguration.getSootClassPath(context);

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

        this.endpointClasses = scene.getClasses().stream()
                .map(EndpointClass::make)
                .filter(Objects::nonNull)
                .toList();

        this.endpointClassMap = endpointClasses.stream().collect(
                Collectors.toMap(EndpointClass::getName, endpointClass -> endpointClass));

        this.endpoints = endpointClasses.stream().flatMap(endpointClass -> endpointClass.getEndpoints().stream())
                .toList();

        this.endpointMap = endpoints.stream().collect(Collectors.toMap(Endpoint::getName, endpoint -> endpoint));
    }

    public EndpointClass getEndpointClassByName(String name) {
        return endpointClassMap.get(name);
    }

    public Endpoint getEndpointByName(String name) {
        return endpointMap.get(name);
    }

    public List<Endpoint> getEndpointByMatcher(EndpointMatcher matcher) {
        return endpoints.stream().filter(matcher::match).toList();
    }


    public static interface EndpointMatcher {
        boolean match(Endpoint endpoint);
    }

    public static class SimpleWildcardMatcher implements EndpointMatcher {
        private final String wildcard;

        public SimpleWildcardMatcher(String wildcard) {
            this.wildcard = wildcard;
        }

        @Override
        public boolean match(Endpoint endpoint) {
            if (wildcard.equals("*")) {
                return true;
            }

            String endPointName = endpoint.getName();
            if (wildcard.endsWith(".*")) {
                String prefix = wildcard.substring(0, wildcard.indexOf(".*"));
                return endPointName.startsWith(prefix);
            } else {
                return endPointName.equals(wildcard);
            }
        }
    }

    public static class OrMatcher implements EndpointMatcher {
        private final List<? extends EndpointMatcher> matchers;

        public OrMatcher(List<? extends EndpointMatcher> matchers) {
            this.matchers = matchers;
        }

        @Override
        public boolean match(Endpoint endpoint) {
            return matchers.stream().anyMatch(matcher -> matcher.match(endpoint));
        }
    }
}
