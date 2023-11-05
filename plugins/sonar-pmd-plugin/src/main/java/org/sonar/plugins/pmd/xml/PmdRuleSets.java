package org.sonar.plugins.pmd.xml;

import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.utils.ValidationMessages;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.plugins.pmd.xml.factory.ActiveRulesRuleSetFactory;
import org.sonar.plugins.pmd.xml.factory.RuleSetFactory;
import org.sonar.plugins.pmd.xml.factory.RulesProfileRuleSetFactory;
import org.sonar.plugins.pmd.xml.factory.XmlRuleSetFactory;

import java.io.IOException;
import java.io.Reader;

/**
 * Convenience class that creates {@link PmdRuleSet} instances out of the given input.
 */
public class PmdRuleSets {

    private static final Logger LOG = Loggers.get(PmdRuleSets.class);

    private PmdRuleSets() {
    }

    /**
     * @param configReader A character stream containing the data of the {@link PmdRuleSet}.
     * @param messages     SonarQube validation messages - allow to inform the enduser about processing problems.
     * @return An instance of PmdRuleSet. The output may be empty but never null.
     */
    public static PmdRuleSet from(Reader configReader, ValidationMessages messages) {
        return createQuietly(new XmlRuleSetFactory(configReader, messages));
    }

    /**
     * @param activeRules   The currently active rules.
     * @param repositoryKey The key identifier of the rule repository.
     * @return An instance of PmdRuleSet. The output may be empty but never null.
     */
    public static PmdRuleSet from(ActiveRules activeRules, String repositoryKey) {
        return create(new ActiveRulesRuleSetFactory(activeRules, repositoryKey));
    }

    /**
     * @param rulesProfile  The current rulesprofile.
     * @param repositoryKey The key identifier of the rule repository.
     * @return An instance of PmdRuleSet. The output may be empty but never null.
     */
    public static PmdRuleSet from(RulesProfile rulesProfile, String repositoryKey) {
        return create(new RulesProfileRuleSetFactory(rulesProfile, repositoryKey));
    }

    private static PmdRuleSet create(RuleSetFactory factory) {
        return factory.create();
    }

    private static PmdRuleSet createQuietly(XmlRuleSetFactory factory) {

        final PmdRuleSet result = create(factory);

        try {
            factory.close();
        } catch (IOException e) {
            LOG.warn("Failed to close the given resource.", e);
        }

        return result;
    }
}
