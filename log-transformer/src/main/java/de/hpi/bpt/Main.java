package de.hpi.bpt;

import de.hpi.bpt.datastructures.EventLog;
import de.hpi.bpt.io.ArffLogWriter;
import de.hpi.bpt.io.CsvLogReader;
import de.hpi.bpt.io.CsvLogWriter;
import de.hpi.bpt.transformation.ActivityAppearanceTransformation;
import de.hpi.bpt.transformation.CaseDurationTransformation;
import de.hpi.bpt.transformation.CaseIdTransformation;
import de.hpi.bpt.transformation.LogTransformer;
import org.apache.commons.lang3.time.StopWatch;

import java.io.File;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;

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

        var endActivityNames = runTimed(() -> extractEndActivityNames(eventLog), "Collecting end activities");
        endActivityNames.forEach(System.out::println);
        var transformer = new LogTransformer(eventLog)
                .with(new CaseIdTransformation())
                .with(new CaseDurationTransformation())
                .with(new ActivityAppearanceTransformation(endActivityNames));
        var caseLog = runTimed(transformer::transform, "Transforming attributes");

        runTimed(() -> new CsvLogWriter().writeToFile(caseLog, "/home/jonas/Downloads/Hospital_caselog.csv"), "Writing CSV file");
        runTimed(() -> new ArffLogWriter().writeToFile(caseLog, "/home/jonas/Downloads/Hospital_caselog.arff"), "Writing ARFF file");
    }

    private static Set<String> extractEndActivityNames(EventLog eventLog) {
        return eventLog.getTyped(eventLog.getSchema().getActivityName(), String.class)
                .getTraces()
                .parallelStream()
                .map(trace -> trace.get(trace.size() - 1))
                .collect(Collectors.toSet());
    }

    private static <T> T runTimed(Supplier<T> function, String message) {
        var stopWatch = start(message);
        var result = function.get();
        stopAndPrint(message, stopWatch);
        return result;
    }

    private static void runTimed(Runnable runnable, String message) {
        var stopWatch = start(message);
        runnable.run();
        stopAndPrint(message, stopWatch);
    }

    private static StopWatch start(String message) {
        StopWatch stopWatch = new StopWatch();
        System.out.println("Starting: " + message);
        stopWatch.start();
        return stopWatch;
    }

    private static void stopAndPrint(String message, StopWatch stopWatch) {
        stopWatch.stop();
        System.out.println("Finished: " + message + " (" + stopWatch.getTime(TimeUnit.MILLISECONDS) + "ms)");
    }
}
