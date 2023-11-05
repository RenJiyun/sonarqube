package org.sonar.plugins.pmd;

import net.sourceforge.pmd.RuleViolation;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.TextRange;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.scanner.ScannerSide;

@ScannerSide
public class PmdViolationRecorder {

    private final FileSystem fs;
    private final ActiveRules activeRules;

    public PmdViolationRecorder(FileSystem fs, ActiveRules activeRules) {
        this.fs = fs;
        this.activeRules = activeRules;
    }

    public void saveViolation(RuleViolation pmdViolation, SensorContext context) {
        final InputFile inputFile = findResourceFor(pmdViolation);
        if (inputFile == null) {
            // Save violations only for existing resources
            return;
        }

        final RuleKey ruleKey = findActiveRuleKeyFor(pmdViolation);

        if (ruleKey == null) {
            // Save violations only for enabled rules
            return;
        }

        final NewIssue issue = context.newIssue()
                .forRule(ruleKey);

        final TextRange issueTextRange = TextRangeCalculator.calculate(pmdViolation, inputFile);

        final NewIssueLocation issueLocation = issue.newLocation()
                .on(inputFile)
                .message(pmdViolation.getDescription())
                .at(issueTextRange);

        issue.at(issueLocation)
                .save();
    }

    private InputFile findResourceFor(RuleViolation violation) {
        return fs.inputFile(
                fs.predicates().hasAbsolutePath(
                        violation.getFilename()
                )
        );
    }

    private RuleKey findActiveRuleKeyFor(RuleViolation violation) {
        final String internalRuleKey = violation.getRule().getName();
        RuleKey ruleKey = RuleKey.of(PmdConstants.REPOSITORY_KEY, internalRuleKey);

        if (activeRules.find(ruleKey) != null) {
            return ruleKey;
        }

        // Let's try the test repo.
        ruleKey = RuleKey.of(PmdConstants.TEST_REPOSITORY_KEY, internalRuleKey);

        return activeRules.find(ruleKey) != null ? ruleKey : null;
    }
}
