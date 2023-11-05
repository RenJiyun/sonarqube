package org.sonar.plugins.pmd.profile;

import org.sonar.api.profiles.ProfileExporter;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.plugins.pmd.PmdConstants;
import org.sonar.plugins.pmd.xml.PmdRuleSet;
import org.sonar.plugins.pmd.xml.PmdRuleSets;

import java.io.Writer;

/**
 * ServerSide component that is able to export all currently active PMD rules as XML.
 */
public class PmdProfileExporter extends ProfileExporter {

    private static final String CONTENT_TYPE_APPLICATION_XML = "application/xml";

    public PmdProfileExporter() {
        super(PmdConstants.REPOSITORY_KEY, PmdConstants.PLUGIN_NAME);
        setSupportedLanguages(PmdConstants.LANGUAGE_KEY);
        setMimeType(CONTENT_TYPE_APPLICATION_XML);
    }

    @Override
    public void exportProfile(RulesProfile profile, Writer writer) {

        final PmdRuleSet tree = PmdRuleSets.from(profile, PmdConstants.REPOSITORY_KEY);

        try {
            tree.writeTo(writer);
        } catch (IllegalStateException e) {
            throw new IllegalStateException("An exception occurred while generating the PMD configuration file from profile: " + profile.getName(), e);
        }
    }
}
