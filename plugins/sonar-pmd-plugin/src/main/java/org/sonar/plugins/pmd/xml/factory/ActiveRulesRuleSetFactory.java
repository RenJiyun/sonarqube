package org.sonar.plugins.pmd.xml.factory;

import org.sonar.api.batch.rule.ActiveRule;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.rules.RulePriority;
import org.sonar.plugins.pmd.PmdLevelUtils;
import org.sonar.plugins.pmd.xml.PmdProperty;
import org.sonar.plugins.pmd.xml.PmdRule;
import org.sonar.plugins.pmd.xml.PmdRuleSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Factory class to create {@link PmdRuleSet} out of {@link org.sonar.api.batch.rule.ActiveRules}.
 */
public class ActiveRulesRuleSetFactory implements RuleSetFactory {

    private final ActiveRules activeRules;
    private final String repositoryKey;

    public ActiveRulesRuleSetFactory(ActiveRules activeRules, String repositoryKey) {
        this.activeRules = activeRules;
        this.repositoryKey = repositoryKey;
    }

    @Override
    public PmdRuleSet create() {

        final Collection<ActiveRule> rules = this.activeRules.findByRepository(repositoryKey);
        PmdRuleSet ruleset = new PmdRuleSet();
        ruleset.setName(repositoryKey);
        ruleset.setDescription(String.format("Sonar Profile: %s", repositoryKey));
        for (ActiveRule rule : rules) {
            String configKey = rule.internalKey();
            PmdRule pmdRule = new PmdRule(configKey, PmdLevelUtils.toLevel(RulePriority.valueOfString(rule.severity())));
            addRuleProperties(rule, pmdRule);
            ruleset.addRule(pmdRule);

            pmdRule.processXpath(rule.internalKey());
        }
        return ruleset;
    }

    private void addRuleProperties(org.sonar.api.batch.rule.ActiveRule activeRule, PmdRule pmdRule) {
        if ((activeRule.params() != null) && !activeRule.params().isEmpty()) {
            List<PmdProperty> properties = new ArrayList<>();
            for (Map.Entry<String, String> activeRuleParam : activeRule.params().entrySet()) {
                properties.add(new PmdProperty(activeRuleParam.getKey(), activeRuleParam.getValue()));
            }
            pmdRule.setProperties(properties);
        }
    }

    @Override
    public void close() {
        // Unnecessary in this class.
    }
}
