package org.sonar.plugins.codeql;

import java.io.IOException;

/**
 * @author renjiyun
 */
public class CodeQLCommandUtils {

    public static void createDatabase(String dbPath) throws IOException, InterruptedException {
        String command = "codeql database create " + dbPath +
                " --language=java " +
                "--command=\"mvn clean install -DskipTests=true\"";

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        Process process = processBuilder.start();
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("create database failed");
        }
    }
}
