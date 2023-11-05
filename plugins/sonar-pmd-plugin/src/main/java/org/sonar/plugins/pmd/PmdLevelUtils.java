package org.sonar.plugins.pmd;

import org.sonar.api.rules.RulePriority;

import javax.annotation.Nullable;
import java.util.Objects;

public final class PmdLevelUtils {

    private static final int INDEX_LEVEL = RulePriority.values().length;
    private PmdLevelUtils() {
        // only static methods
    }

    public static RulePriority fromLevel(@Nullable Integer level) {

        if (Objects.isNull(level)) {
            return null;
        }

        final int index = Math.abs(INDEX_LEVEL - level);

        return (index < INDEX_LEVEL) ? RulePriority.valueOfInt(index) : null;
    }

    public static Integer toLevel(RulePriority priority) {
        return Math.abs(priority.ordinal() - INDEX_LEVEL);
    }
}
