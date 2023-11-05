package org.sonar.plugins.pmd.xml.factory;

import org.sonar.plugins.pmd.xml.PmdRuleSet;

import java.io.Closeable;

/**
 * Interface for all RuleSetFactories.
 */
public interface RuleSetFactory extends Closeable {

    /**
     * @return A PMD Ruleset.
     */
    PmdRuleSet create();
}
