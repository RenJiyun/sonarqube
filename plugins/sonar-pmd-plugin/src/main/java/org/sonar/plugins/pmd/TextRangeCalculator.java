package org.sonar.plugins.pmd;

import net.sourceforge.pmd.RuleViolation;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.TextPointer;
import org.sonar.api.batch.fs.TextRange;

/**
 * Calculates a {@link org.sonar.api.batch.fs.TextRange} for a given {@link net.sourceforge.pmd.RuleViolation}.
 */
class TextRangeCalculator {

    private TextRangeCalculator() {
    }

    static TextRange calculate(RuleViolation pmdViolation, InputFile inputFile) {
        final int startLine = calculateBeginLine(pmdViolation);
        final int endLine = calculateEndLine(pmdViolation);

        // PMD counts TABs differently, so we can not use RuleViolation#getBeginColumn and RuleViolation#getEndColumn
        // Therefore, we select complete lines.
        final TextPointer startPointer = inputFile.selectLine(startLine).start();
        final TextPointer endPointer = inputFile.selectLine(endLine).end();

        return inputFile.newRange(startPointer, endPointer);
    }

    /**
     * Calculates the endLIne of a violation report.
     *
     * @param pmdViolation The violation for which the endLine should be calculated.
     * @return The endLine is assumed to be the line with the biggest number.
     */
    private static int calculateEndLine(RuleViolation pmdViolation) {
        return Math.max(pmdViolation.getBeginLine(), pmdViolation.getEndLine());
    }

    /**
     * Calculates the beginLine of a violation report.
     *
     * @param pmdViolation The violation for which the beginLine should be calculated.
     * @return The beginLine is assumed to be the line with the smallest number. However, if the smallest number is
     * out-of-range (non-positive), it takes the other number.
     */
    private static int calculateBeginLine(RuleViolation pmdViolation) {
        int minLine = Math.min(pmdViolation.getBeginLine(), pmdViolation.getEndLine());
        return minLine > 0 ? minLine : calculateEndLine(pmdViolation);
    }
}
