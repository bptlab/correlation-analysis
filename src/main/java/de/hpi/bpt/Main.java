package de.hpi.bpt;

import de.hpi.bpt.io.CsvLogReader;
import de.hpi.bpt.io.CsvLogWriter;
import de.hpi.bpt.transformation.ExistingAttributeTransformation;
import org.apache.commons.lang3.time.StopWatch;

import java.io.File;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class Main {

    private static final String CSV_FILE_NAME = "/home/jonas/Downloads/Hospital_log_typed.csv";
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
    private static final char SEPARATOR = ';';

    private static final String CASE_ID_NAME = "caseid";
    private static final String TIMESTAMP_NAME = "timestamp";
    private static final String ACTIVITY_NAME = "activity";

    public static void main(String[] args) {

        var file = new File(CSV_FILE_NAME);
        var reader = new CsvLogReader()
                .separator(SEPARATOR)
                .caseIdName(CASE_ID_NAME)
                .timestampName(TIMESTAMP_NAME)
                .activityName(ACTIVITY_NAME);

        var eventLog = runTimed(() -> reader.read(file), "Reading CSV file");

        var transformation = new ExistingAttributeTransformation(eventLog);
        var caseLog = runTimed(transformation::transform, "Transforming attributes");

        var writer = new CsvLogWriter();
        runTimed(() -> writer.writeToFile(caseLog, "/home/jonas/Downloads/Hospital_caselog.csv"), "Writing CSV file");
    }

    private static <T> T runTimed(Supplier<T> function, String message) {
        StopWatch stopWatch = new StopWatch();
        System.out.println("Starting: " + message);
        stopWatch.start();
        var result = function.get();
        stopWatch.stop();
        System.out.println("Finished: " + message + " (" + stopWatch.getTime(TimeUnit.MILLISECONDS) + "ms)");
        return result;
    }

    private static void runTimed(Runnable runnable, String message) {
        StopWatch stopWatch = new StopWatch();
        System.out.println("Starting: " + message);
        stopWatch.start();
        runnable.run();
        stopWatch.stop();
        System.out.println("Finished: " + message + " (" + stopWatch.getTime(TimeUnit.MILLISECONDS) + "ms)");
    }
}
