package org.sonar.plugins.pmd;

import org.sonar.api.server.profile.BuiltInQualityProfilesDefinition;

public class PmdBuiltInQualityProfileDefinition implements BuiltInQualityProfilesDefinition {
    @Override
    public void define(Context context) {
        NewBuiltInQualityProfile profile = context.createBuiltInQualityProfile("Built in QP for Pmd", "pmd");
        profile.done();
    }
}
