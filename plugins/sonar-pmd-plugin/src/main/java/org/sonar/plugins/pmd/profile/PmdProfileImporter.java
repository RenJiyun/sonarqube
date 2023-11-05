package org.sonar.plugins.pmd.profile;

import org.sonar.api.profiles.ProfileImporter;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.rules.ActiveRule;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RuleFinder;
import org.sonar.api.rules.RuleQuery;
import org.sonar.api.utils.ValidationMessages;
import org.sonar.plugins.pmd.PmdConstants;
import org.sonar.plugins.pmd.PmdLevelUtils;
import org.sonar.plugins.pmd.xml.PmdProperty;
import org.sonar.plugins.pmd.xml.PmdRule;
import org.sonar.plugins.pmd.xml.PmdRuleSet;
import org.sonar.plugins.pmd.xml.PmdRuleSets;

import java.io.Reader;

public class PmdProfileImporter extends ProfileImporter {

    private final RuleFinder ruleFinder;

    public PmdProfileImporter(RuleFinder ruleFinder) {
        super(PmdConstants.REPOSITORY_KEY, PmdConstants.PLUGIN_NAME);
        setSupportedLanguages(PmdConstants.LANGUAGE_KEY);
        this.ruleFinder = ruleFinder;
    }

    private void setParameters(ActiveRule activeRule, PmdRule pmdRule, Rule rule, ValidationMessages messages) {
        for (PmdProperty prop : pmdRule.getProperties()) {
            String paramName = prop.getName();
            if (rule.getParam(paramName) == null) {
                messages.addWarningText("The property '" + paramName + "' is not supported in the pmd rule: " + pmdRule.getRef());
            } else {
                activeRule.setParameter(paramName, prop.getValue());
            }
        }
    }

    @Override
    public RulesProfile importProfile(Reader pmdConfigurationFile, ValidationMessages messages) {
        PmdRuleSet pmdRuleset = PmdRuleSets.from(pmdConfigurationFile, messages);
        RulesProfile profile = RulesProfile.create();
        for (PmdRule pmdRule : pmdRuleset.getPmdRules()) {
            String ruleClassName = pmdRule.getClazz();
            if (PmdConstants.XPATH_CLASS.equals(ruleClassName)) {
                messages.addWarningText("PMD XPath rule '" + pmdRule.getName()
                        + "' can't be imported automatically. The rule must be created manually through the SonarQube web interface.");
            } else {
                String ruleRef = pmdRule.getRef();
                if (ruleRef == null) {
                    messages.addWarningText("A PMD rule without 'ref' attribute can't be imported. see '" + ruleClassName + "'");
                } else {
                    Rule rule = ruleFinder.find(RuleQuery.create().withRepositoryKey(PmdConstants.REPOSITORY_KEY).withConfigKey(ruleRef));
                    if (rule != null) {
                        ActiveRule activeRule = profile.activateRule(rule, PmdLevelUtils.fromLevel(pmdRule.getPriority()));
                        setParameters(activeRule, pmdRule, rule, messages);
                    } else {
                        messages.addWarningText("Unable to import unknown PMD rule '" + ruleRef + "'");
                    }
                }
            }
        }
        return profile;
    }
}
