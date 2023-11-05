package org.sonar.plugins.pmd.rule;

import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.plugins.pmd.PmdConstants;

import javax.naming.Context;

public final class PmdUnitTestsRulesDefinition implements RulesDefinition {

    public PmdUnitTestsRulesDefinition() {
        // Do nothing
    }

    @Override
    public void define(Context context) {
        NewRepository repository = context
                .createRepository(PmdConstants.TEST_REPOSITORY_KEY, PmdConstants.LANGUAGE_KEY)
                .setName(PmdConstants.TEST_REPOSITORY_NAME);

        PmdRulesDefinition.extractRulesData(repository, "/org/sonar/plugins/pmd/rules-unit-tests.xml", "/org/sonar/l10n/pmd/rules/pmd-unit-tests");

        repository.done();
    }
}
