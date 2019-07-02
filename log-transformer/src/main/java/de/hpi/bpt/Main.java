package de.hpi.bpt;

import de.hpi.bpt.datastructures.EventLog;
import de.hpi.bpt.io.ArffLogWriter;
import de.hpi.bpt.io.CsvCaseLogReader;
import de.hpi.bpt.io.CsvLogReader;
import org.apache.commons.lang3.time.StopWatch;

import java.io.File;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class Main {

    private static final String EVENT_FILE_NAME = "/home/jonas/Data/Macif/Demands_Joined.csv";
    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
    private static final char SEPARATOR = ',';

    private static final String CASE_ID_NAME = "CaseID";
    private static final String TIMESTAMP_NAME = "Timestamp";
    private static final String ACTIVITY_NAME = "EventName";

    public static void main(String[] args) {

        var csvLogReader = new CsvLogReader()
                .separator(SEPARATOR)
                .dateFormat(DATE_FORMAT)
                .caseIdName(CASE_ID_NAME)
                .timestampName(TIMESTAMP_NAME)
                .activityName(ACTIVITY_NAME);

        var caseLog = runTimed(() -> new CsvCaseLogReader(csvLogReader).read(new File(EVENT_FILE_NAME)), "Reading event log");

//        var endActivityNames = runTimed(() -> extractEndActivityNames(eventLog), "Collecting end activities");
//        var transformer = new LogTransformer(eventLog)
//                .with(new CaseIdTransformation())
//                .with(new CaseDurationTransformation())
//                .with(new ActivityAppearanceTransformation(endActivityNames));
//        var transformedLog = runTimed(transformer::transform, "Transforming attributes");
//
//        runTimed(() -> new CsvLogWriter().writeToFile(transformedLog, "/home/jonas/Data/Macif/Demands_CaseLog.csv"), "Writing CSV file");
        runTimed(() -> new ArffLogWriter().writeToFile(caseLog, "/home/jonas/Data/Macif/Demands_Joined.arff"), "Writing ARFF file");
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
