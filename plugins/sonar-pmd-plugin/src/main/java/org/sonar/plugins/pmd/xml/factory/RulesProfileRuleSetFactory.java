package org.sonar.plugins.pmd.xml.factory;

import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.rules.ActiveRule;
import org.sonar.api.rules.ActiveRuleParam;
import org.sonar.plugins.pmd.PmdLevelUtils;
import org.sonar.plugins.pmd.xml.PmdProperty;
import org.sonar.plugins.pmd.xml.PmdRule;
import org.sonar.plugins.pmd.xml.PmdRuleSet;

import java.util.ArrayList;
import java.util.List;

/**
 * Factory class to create {@link PmdRuleSet} out of {@link org.sonar.api.profiles.RulesProfile}.
 */
public class RulesProfileRuleSetFactory implements RuleSetFactory {

    private final RulesProfile rulesProfile;
    private final String repositoryKey;

    public RulesProfileRuleSetFactory(RulesProfile rulesProfile, String repositoryKey) {
        this.rulesProfile = rulesProfile;
        this.repositoryKey = repositoryKey;
    }

    @Override
    public PmdRuleSet create() {

        final PmdRuleSet ruleset = new PmdRuleSet();
        ruleset.setName(repositoryKey);
        ruleset.setDescription(String.format("Sonar Profile: %s", repositoryKey));

        final List<ActiveRule> activeRules = rulesProfile.getActiveRulesByRepository(repositoryKey);

        for (ActiveRule activeRule : activeRules) {
            if (activeRule.getRule().getRepositoryKey().equals(repositoryKey)) {
                String configKey = activeRule.getRule().getConfigKey();
                PmdRule rule = new PmdRule(configKey, PmdLevelUtils.toLevel(activeRule.getSeverity()));
                addRuleProperties(activeRule, rule);
                ruleset.addRule(rule);
                rule.processXpath(activeRule.getRuleKey());
            }
        }

        return ruleset;
    }

    private void addRuleProperties(ActiveRule activeRule, PmdRule pmdRule) {
        if ((activeRule.getActiveRuleParams() != null) && !activeRule.getActiveRuleParams().isEmpty()) {
            List<PmdProperty> properties = new ArrayList<>();
            for (ActiveRuleParam activeRuleParam : activeRule.getActiveRuleParams()) {
                properties.add(new PmdProperty(activeRuleParam.getRuleParam().getKey(), activeRuleParam.getValue()));
            }
            pmdRule.setProperties(properties);
        }
    }

    @Override
    public void close() {
        // Unnecessary in this class.
    }
}
