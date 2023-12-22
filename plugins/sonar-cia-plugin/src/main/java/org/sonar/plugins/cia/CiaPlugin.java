package org.sonar.plugins.cia;

import org.sonar.api.Plugin;

/**
 * 变更影响分析插件
 *
 * @author renjiyun
 */
public class CiaPlugin implements Plugin {
    @Override
    public void define(Context context) {
        context.addExtensions(
                CiaExecutor.class,
                CiaConfiguration.class,
                CiaSensor.class,
                EndpointRepository.class
        );
    }
}
