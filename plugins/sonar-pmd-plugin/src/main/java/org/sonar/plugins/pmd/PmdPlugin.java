package org.sonar.plugins.pmd;

import org.sonar.api.Plugin;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.plugins.pmd.profile.PmdProfileExporter;
import org.sonar.plugins.pmd.profile.PmdProfileImporter;
import org.sonar.plugins.pmd.rule.PmdRulesDefinition;
import org.sonar.plugins.pmd.rule.PmdUnitTestsRulesDefinition;

/**
 * The {@link PmdPlugin} is the main entry-point of Sonar-PMD.
 */
public class PmdPlugin implements Plugin {

    @Override
    public void define(Context context) {
        context.addExtensions(
                PropertyDefinition.builder(PmdConfiguration.PROPERTY_GENERATE_XML)
                        .defaultValue("false")
                        .name("Generate XML Report")
                        .hidden()
                        .build(),

                PmdSensor.class,
                PmdConfiguration.class,
                PmdExecutor.class,
                PmdRulesDefinition.class,
                PmdUnitTestsRulesDefinition.class,
                PmdProfileExporter.class,
                PmdProfileImporter.class,
                PmdViolationRecorder.class
        );
    }
}
