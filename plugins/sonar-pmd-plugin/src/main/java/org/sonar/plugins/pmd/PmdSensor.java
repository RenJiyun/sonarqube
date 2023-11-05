package org.sonar.plugins.pmd;

import net.sourceforge.pmd.RuleViolation;
import org.sonar.api.batch.fs.FilePredicates;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile.Type;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;

public class PmdSensor implements Sensor {
    private final ActiveRules profile;
    private final PmdExecutor executor;
    private final PmdViolationRecorder pmdViolationRecorder;
    private final FileSystem fs;

    public PmdSensor(ActiveRules profile, PmdExecutor executor, PmdViolationRecorder pmdViolationRecorder, FileSystem fs) {
        this.profile = profile;
        this.executor = executor;
        this.pmdViolationRecorder = pmdViolationRecorder;
        this.fs = fs;
    }

    private boolean shouldExecuteOnProject() {
        return (hasFilesToCheck(Type.MAIN, PmdConstants.REPOSITORY_KEY))
                || (hasFilesToCheck(Type.TEST, PmdConstants.TEST_REPOSITORY_KEY));
    }

    private boolean hasFilesToCheck(Type type, String repositoryKey) {
        FilePredicates predicates = fs.predicates();
        final boolean hasMatchingFiles = fs.hasFiles(predicates.and(
                predicates.hasLanguage(PmdConstants.LANGUAGE_KEY),
                predicates.hasType(type)));
        return hasMatchingFiles && !profile.findByRepository(repositoryKey).isEmpty();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    @Override
    public void describe(SensorDescriptor descriptor) {
        descriptor.onlyOnLanguage(PmdConstants.LANGUAGE_KEY)
                .name("PmdSensor");
    }

    @Override
    public void execute(SensorContext context) {
        if (shouldExecuteOnProject()) {
            for (RuleViolation violation : executor.execute()) {
                pmdViolationRecorder.saveViolation(violation, context);
            }
        }
    }
}
