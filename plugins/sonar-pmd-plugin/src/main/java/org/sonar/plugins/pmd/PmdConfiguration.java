package org.sonar.plugins.pmd;

import net.sourceforge.pmd.Report;
import net.sourceforge.pmd.renderers.Renderer;
import net.sourceforge.pmd.renderers.XMLRenderer;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.config.Configuration;
import org.sonar.api.scanner.ScannerSide;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;

@ScannerSide
public class PmdConfiguration {
    static final String PROPERTY_GENERATE_XML = "sonar.pmd.generateXml";
    private static final String PMD_RESULT_XML = "pmd-result.xml";
    private static final Logger LOG = Loggers.get(PmdConfiguration.class);
    private final FileSystem fileSystem;
    private final Configuration settings;

    public PmdConfiguration(FileSystem fileSystem, Configuration settings) {
        this.fileSystem = fileSystem;
        this.settings = settings;
    }

    private static String reportToString(Report report) throws IOException {
        StringWriter output = new StringWriter();

        Renderer xmlRenderer = new XMLRenderer();
        xmlRenderer.setWriter(output);
        xmlRenderer.start();
        xmlRenderer.renderFileReport(report);
        xmlRenderer.end();

        return output.toString();
    }

    File dumpXmlRuleSet(String repositoryKey, String rulesXml) {
        try {
            File configurationFile = writeToWorkingDirectory(rulesXml, repositoryKey + ".xml").toFile();

            LOG.info("PMD configuration: " + configurationFile.getAbsolutePath());

            return configurationFile;
        } catch (IOException e) {
            throw new IllegalStateException("Fail to save the PMD configuration", e);
        }
    }

    /**
     * Writes an XML Report about the analyzed project into the current working directory
     * unless <code>sonar.pmd.generateXml</code> is set to false.
     *
     * @param report The report which shall be written into an XML file.
     * @return The file reference to the XML document.
     */
    Path dumpXmlReport(Report report) {
        if (!settings.getBoolean(PROPERTY_GENERATE_XML).orElse(false)) {
            return null;
        }

        try {
            final String reportAsString = reportToString(report);
            final Path reportFile = writeToWorkingDirectory(reportAsString, PMD_RESULT_XML);

            LOG.info("PMD output report: " + reportFile.toString());

            return reportFile;
        } catch (IOException e) {
            throw new IllegalStateException("Fail to save the PMD report", e);
        }
    }

    private Path writeToWorkingDirectory(String content, String fileName) throws IOException {
        final Path targetPath = fileSystem.workDir().toPath().resolve(fileName);
        Files.write(targetPath, content.getBytes());

        return targetPath;
    }
}
