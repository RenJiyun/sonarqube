package org.sonar.plugins.cia;

import org.sonar.api.scanner.ScannerSide;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import soot.AmbiguousMethodException;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.tagkit.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ScannerSide
public class CiaExecutor {
    private static final Logger LOGGER = Loggers.get(CiaExecutor.class);

    private Scene scene;
    private final Map<String, SootClass> endpointPrefixToClass = new HashMap<>();

    public void init(Scene scene, List<String> processDirs, String sootClassPath) {
        this.scene = scene;
        SootUtils.loadClasses(scene, processDirs, sootClassPath);

        // todo: 假设这里只有应用类, 如果不是, 需要将类库中的类排除出去, 以便减少分析的类数量
        for (SootClass sootClass : scene.getClasses()) {
            SootClass superClass = sootClass.getSuperclass();
            if (CiaConstants.ENDPOINT_SUPER_CLASS.equals(SootUtils.getQualifiedName(superClass))) {
                String endpointPrefix = getEndpointPrefix(sootClass);
                if (endpointPrefix == null || endpointPrefix.isEmpty()) {
                    LOGGER.warn("service class {} has no endpoint prefix", SootUtils.getQualifiedName(sootClass));
                    continue;
                }
                endpointPrefixToClass.put(getEndpointPrefix(sootClass), sootClass);
            }
        }
    }

    private String getEndpointPrefix(SootClass sootClass) {
        for (Tag tag : sootClass.getTags()) {
            if (tag instanceof VisibilityAnnotationTag visibilityAnnotationTag) {
                List<AnnotationTag> annotationTags = visibilityAnnotationTag.getAnnotations();
                for (AnnotationTag annotationTag : annotationTags) {
                    if (CiaConstants.SPRING_SERVICE_ANNOTATION.equals(annotationTag.getType())) {
                        for (AnnotationElem ele : annotationTag.getElems()) {
                            if (ele instanceof AnnotationStringElem stringElem) {
                                if (CiaConstants.SPRING_SERVICE_ANNOTATION_VALUE.equals(stringElem.getName())) {
                                    return stringElem.getValue();
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    public void execute(Map<String, SootMethod> endpointsToBeChecked) {
        CallGraph cg = SootUtils.getCallGraph(scene, endpointsToBeChecked.values());
        System.out.println(cg.size());
    }


    /**
     * 获取用户指定的所有需要检查的端点方法, 这些方法之后将成为 call graph analysis 的入口方法
     *
     * @param endpointsToBeChecked
     * @return
     */
    public Map<String, SootMethod> parseEndpoints(List<String> endpointsToBeChecked) {
        Map<String, SootMethod> endpoints = new HashMap<>();

        boolean isAll = endpointsToBeChecked.stream().anyMatch(e -> e.equals("*"));
        if (isAll) {
            for (Map.Entry<String, SootClass> entry : endpointPrefixToClass.entrySet()) {
                addAllEndpoints(entry.getValue(), entry.getKey(), endpoints);
            }
            return endpoints;
        }

        for (String endpointToBeChecked : endpointsToBeChecked) {
            if (endpointToBeChecked.endsWith(".*")) {
                String prefix = endpointToBeChecked.substring(0, endpointToBeChecked.indexOf(".*"));
                for (String endpointPrefix : endpointPrefixToClass.keySet()) {
                    if (endpointPrefix.equals(prefix)) {
                        SootClass targetClass = endpointPrefixToClass.get(endpointPrefix);
                        addAllEndpoints(targetClass, endpointPrefix, endpoints);
                    }
                }
            } else {
                String endpointPrefix = endpointToBeChecked.substring(0, endpointToBeChecked.lastIndexOf("."));
                String endpoint = endpointToBeChecked.substring(endpointToBeChecked.lastIndexOf(".") + 1);
                SootClass sootClass = endpointPrefixToClass.get(endpointPrefix);
                if (sootClass == null) {
                    LOGGER.warn("endpoint {} not found", endpointToBeChecked);
                    continue;
                }
                addEndpoint(sootClass, endpointPrefix, endpoint, endpoints);
            }
        }

        return endpoints;
    }

    private void addAllEndpoints(SootClass targetClass, String endpointPrefix, Map<String, SootMethod> endpoints) {
        List<SootMethod> endpointMethodsInTargetClass = targetClass.getMethods().stream().filter(this::isValidEndpoint).toList();
        for (SootMethod endpointMethod : endpointMethodsInTargetClass) {
            String fullEndpoint = endpointPrefix + "." + endpointMethod.getName();
            endpoints.put(fullEndpoint, endpointMethod);
        }
    }

    private void addEndpoint(SootClass sootClass, String endpointPrefix, String endpoint, Map<String, SootMethod> endpoints) {
        String fullEndpoint = endpointPrefix + "." + endpoint;

        SootMethod endpointMethod = null;
        try {
            endpointMethod = sootClass.getMethodByName(endpoint);
        } catch (AmbiguousMethodException e) {
            List<SootMethod> candidates = sootClass.getMethods().stream().filter(m -> m.getName().equals(endpoint)).toList();
            for (SootMethod candidate : candidates) {
                if (isValidEndpoint(candidate)) {
                    endpointMethod = candidate;
                    break;
                }
            }
        }

        if (endpointMethod == null) {
            LOGGER.warn("endpoint {} not found", fullEndpoint);
            return;
        }

        endpoints.put(fullEndpoint, endpointMethod);
    }

    private boolean isValidEndpoint(SootMethod candidate) {
        return candidate.getSubSignature().startsWith(CiaConstants.ENDPOINT_RETURN_TYPE);
    }
}
