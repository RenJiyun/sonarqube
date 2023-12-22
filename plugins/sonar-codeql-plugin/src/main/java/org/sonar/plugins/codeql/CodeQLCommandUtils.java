package org.sonar.plugins.codeql;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * @author renjiyun
 */
public class CodeQLCommandUtils {
    private static final String JAVA8_HOME = "/home/ren/.sdkman/candidates/java/8.0.382-albba";

    public static void createDatabase(String dbPath, String workDir) throws IOException, InterruptedException {
        String command = "codeql database create " + dbPath + " " +
                "--overwrite " +
                "--language=java " +
                "--command=\"mvn clean install -DskipTests=true\"";

        buildAndRunProcess(command, workDir, "create database");
    }

    public static void runQuery(String dbPath, String qlPath, String outputPath, String workDir)
            throws IOException, InterruptedException {
        String command = "codeql query run " +
                "--database " + dbPath + " " +
                "--output " + outputPath + " " +
                "--threads=8 " +
                "-- " + qlPath;
        buildAndRunProcess(command, workDir, "run query");
    }

    public static void bqrs2csv(String bqrsFilePath, String csvFilePath, String workDir)
            throws IOException, InterruptedException {
        String command = "codeql bqrs decode " +
                "--format=csv " +
                "--output=" + csvFilePath + " " +
                "-- " + bqrsFilePath;
        buildAndRunProcess(command, workDir, "bqrs to csv");
    }

    private static void buildAndRunProcess(String command, String workDir, String functionName) {
        ProcessBuilder processBuilder = new ProcessBuilder("/bin/bash", "-c", command);
        processBuilder.directory(new File(workDir));
        Map<String, String> env = processBuilder.environment();
        env.put("JAVA_HOME", JAVA8_HOME);
        processBuilder.inheritIO();
        int exitCode;
        try {
            Process process = processBuilder.start();
            exitCode = process.waitFor();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        if (exitCode != 0) {
            throw new RuntimeException(functionName + " failed");
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        // createDatabase("/home/ren/.tmp/cq/db/wlzq-activity-db",
        // "/home/ren/.tmp/cq/wlzq-activity");
        runQuery("/home/ren/.tmp/cq/db/wlzq-activity-db",
                "/home/ren/work/code/sonarqube/plugins/sonar-codeql-plugin/src/main/resources/wlzq-ql/DependsOn.ql",
                "/home/ren/work/code/sonarqube/plugins/sonar-codeql-plugin/src/main/resources/wlzq-ql/output/DependsOn.bqrs",
                "/tmp");
        bqrs2csv(
                "/home/ren/work/code/sonarqube/plugins/sonar-codeql-plugin/src/main/resources/wlzq-ql/output/DependsOn.bqrs",
                "/home/ren/work/code/sonarqube/plugins/sonar-codeql-plugin/src/main/resources/wlzq-ql/output/DependsOn.csv",
                "/tmp");
    }
}
