package org.sonar.plugins.cia;

import soot.SootMethod;

/**
 * @author renjiyun
 */
public class Endpoint {
    private final String name;
    private final SootMethod sootMethod;

    public Endpoint(String name, SootMethod sootMethod) {
        this.name = name;
        this.sootMethod = sootMethod;
    }

    public String getName() {
        return name;
    }

    public SootMethod getSootMethod() {
        return sootMethod;
    }

}
