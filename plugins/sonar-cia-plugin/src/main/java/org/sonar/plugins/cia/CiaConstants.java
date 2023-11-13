package org.sonar.plugins.cia;

public class CiaConstants {
    public static final String LANGUAGE_KEY = "java";
    public static final String ENDPOINTS_TO_BE_CHECKED = "org.sonar.plugins.cia.endpoints";
    public static final String SPRING_SERVICE_ANNOTATION = "Lorg/springframework/stereotype/Service;";
    public static final String SPRING_SERVICE_ANNOTATION_VALUE = "value";

    ///////////////////////////////////////////////////////////////////////////////////////////
    // wlzq 相关
    /** 接口类父类名 */
    public static final String ENDPOINT_SUPER_CLASS = "com.wlzq.core.BaseService";
    public static final String ENDPOINT_RETURN_TYPE = "com.wlzq.core.dto.ResultDto";

    ///////////////////////////////////////////////////////////////////////////////////////////
}
