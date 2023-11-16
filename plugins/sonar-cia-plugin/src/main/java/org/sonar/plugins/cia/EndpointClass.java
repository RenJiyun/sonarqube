package org.sonar.plugins.cia;

import soot.SootClass;
import soot.SootMethod;
import soot.tagkit.AnnotationStringElem;
import soot.tagkit.AnnotationTag;
import soot.tagkit.VisibilityAnnotationTag;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author renjiyun
 */
public class EndpointClass {
    private static final String ENDPOINT_SUPER_CLASS = "com.wlzq.core.BaseService";
    private static final String SPRING_SERVICE_ANNOTATION = "Lorg/springframework/stereotype/Service;";
    private static final String SPRING_SERVICE_ANNOTATION_VALUE = "value";

    private static final String ENDPOINT_RETURN_TYPE = "com.wlzq.core.dto.ResultDto";

    private final SootClass sootClass;
    private final String name;

    private List<Endpoint> endpoints;
    private Map<String, Endpoint> endpointMap;

    public static EndpointClass make(SootClass sootClass) {
        if (sootClass == null) {
            return null;
        }

        String name = getEndpointClassName(sootClass);
        if (name == null || name.isEmpty()) {
            return null;
        }

        EndpointClass endpointClass = new EndpointClass(sootClass, name);
        endpointClass.init();
        return endpointClass;
    }


    private EndpointClass(SootClass sootClass, String name) {
        this.sootClass = sootClass;
        this.name = name;
    }

    private void init() {
        this.endpoints = sootClass.getMethods().stream()
                .filter(this::isValidEndpoint)
                .map(method -> new Endpoint(name + "." + method.getName(), method))
                .toList();

        this.endpointMap = endpoints.stream().collect(Collectors.toMap(Endpoint::getName, endpoint -> endpoint));
    }

    private static String getEndpointClassName(SootClass sootClass) {
        if (!sootClass.getSuperclass().getName().equals(ENDPOINT_SUPER_CLASS)) {
            return null;
        }

        List<VisibilityAnnotationTag> visibilityAnnotationTags = sootClass.getTags().stream()
                .filter(tag -> tag instanceof VisibilityAnnotationTag)
                .map(tag -> (VisibilityAnnotationTag) tag)
                .toList();

        Optional<AnnotationTag> springServiceAnnotationTag = visibilityAnnotationTags.stream()
                .flatMap(tag -> tag.getAnnotations().stream())
                .filter(annotationTag -> SPRING_SERVICE_ANNOTATION.equals(annotationTag.getType()))
                .findFirst();

        if (springServiceAnnotationTag.isPresent()) {
            Optional<AnnotationStringElem> annotationStringElem = springServiceAnnotationTag.get().getElems().stream()
                    .filter(elem -> elem instanceof AnnotationStringElem)
                    .map(elem -> (AnnotationStringElem) elem)
                    .filter(elem -> SPRING_SERVICE_ANNOTATION_VALUE.equals(elem.getName()))
                    .findFirst();
            if (annotationStringElem.isPresent()) {
                return annotationStringElem.get().getValue();
            }
        }
        return null;
    }

    private boolean isValidEndpoint(SootMethod candidate) {
        return candidate.isPublic() && candidate.getReturnType().toString().equals(ENDPOINT_RETURN_TYPE);
    }

    public String getName() {
        return name;
    }

    public List<Endpoint> getEndpoints() {
        return endpoints;
    }

}
