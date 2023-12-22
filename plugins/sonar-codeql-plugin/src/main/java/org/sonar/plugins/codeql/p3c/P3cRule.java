package org.sonar.plugins.codeql.p3c;

import lombok.Getter;
import lombok.Setter;

/**
 * @author renjiyun
 */
@Getter
@Setter
public class P3cRule {
    private String name;
    private String description;
    private String[] tags;
    private String severity;
    private String type;
    private String category;
}
