package org.sonar.plugins.codeql.p3c;

import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.scanner.sensor.ProjectSensor;
import org.sonar.plugins.codeql.Constants;

/**
 * @author renjiyun
 */
public class P3cSensor implements ProjectSensor {
    @Override
    public void describe(SensorDescriptor descriptor) {
        descriptor.onlyOnLanguage(Constants.LANGUAGE)
                .name(Constants.PLUGIN_PREFIX + this.getClass().getSimpleName());
    }

    @Override
    public void execute(SensorContext context) {
        // 1. 生成 codeql 数据库
        // 2. 执行 ql 脚本, 生成格式为 bqrs 的结果
        // 3. 将 bqrs 结果转换成 csv 格式
    }
}
