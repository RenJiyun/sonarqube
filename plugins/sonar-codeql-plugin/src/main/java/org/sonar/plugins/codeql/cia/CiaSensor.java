package org.sonar.plugins.codeql.cia;

import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.plugins.codeql.Constants;

/**
 * @author renjiyun
 */
public class CiaSensor implements Sensor {
    @Override
    public void describe(SensorDescriptor descriptor) {
        descriptor.onlyOnLanguage(Constants.LANGUAGE)
                .name(Constants.PLUGIN_PREFIX + this.getClass().getSimpleName());
    }

    @Override
    public void execute(SensorContext context) {

    }
}
