package org.sonar.plugins.pmd;

/**
 * Common configuration parameters for the Sonar-PMD plugin.
 */
public final class PmdConstants {
    public static final String PLUGIN_NAME = "PMD";
    public static final String PLUGIN_KEY = "pmd";
    public static final String REPOSITORY_KEY = PLUGIN_KEY;
    public static final String REPOSITORY_NAME = "PMD";
    public static final String TEST_REPOSITORY_KEY = "pmd-unit-tests";
    public static final String TEST_REPOSITORY_NAME = "PMD Unit Tests";
    public static final String XPATH_CLASS = "net.sourceforge.pmd.lang.rule.XPathRule";
    public static final String XPATH_EXPRESSION_PARAM = "xpath";
    public static final String XPATH_MESSAGE_PARAM = "message";

    /**
     * Key of the java version used for sources
     */
    public static final String JAVA_SOURCE_VERSION = "sonar.java.source";

    /**
     * Default value for property {@link #JAVA_SOURCE_VERSION}.
     */
    public static final String JAVA_SOURCE_VERSION_DEFAULT_VALUE = "1.6";

    /**
     * The Java Language key.
     */
    public static final String LANGUAGE_KEY = "java";

    private PmdConstants() {
    }
}
