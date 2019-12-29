package de.hpi.bpt.logtransformer;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;

/**
 * Parses configuration file, if provided, and starts the transformation.
 */
public class Main {

    public static void main(String[] args) throws IOException {
        Project project;

        if (args.length == 0) {
            project = Project.BPIC2019();
        } else {
            var configFile = new File(args[0]);
            if (configFile.isFile()) {
                project = new ObjectMapper().readValue(configFile, Project.class);
            } else {
                throw new RuntimeException("Provided configuration file does not exist.");
            }
        }

        var logTransformRunner = new LogTransformRunner(project);
        logTransformRunner.run();
    }
}
