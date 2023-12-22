package org.sonar.plugins.codeql.p3c;

import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.plugins.codeql.Constants;

import java.util.ArrayList;
import java.util.List;

public class P3cRulesDefinition implements RulesDefinition {
    @Override
    public void define(Context context) {
        NewRepository repository = context
                .createRepository(Constants.P3C_REPOSITORY_KEY, Constants.LANGUAGE)
                .setName(Constants.P3C_REPOSITORY_NAME);
        List<P3cRule> p3cRules = parseP3cRulesFromMd();
        p3cRules.forEach(rule -> doDefineRule(repository, rule));
        repository.done();
    }

    private List<P3cRule> parseP3cRulesFromMd() {
        return new ArrayList<>();
    }

    private void doDefineRule(NewRepository repository, P3cRule p3cRule) {

    }

}
