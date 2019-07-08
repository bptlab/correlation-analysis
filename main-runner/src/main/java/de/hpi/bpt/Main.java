package de.hpi.bpt;

import de.hpi.bpi.ModelAnalyzer;
import de.hpi.bpt.datastructures.EventLog;
import de.hpi.bpt.io.*;
import de.hpi.bpt.transformation.LogTransformer;
import de.hpi.bpt.transformation.time.CaseDurationTransformation;
import de.hpi.bpt.transformation.time.ParallelCaseCountTransformation;
import org.apache.commons.lang3.time.StopWatch;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class Main {

    private static final String MODEL_FILE = "/home/jonas/Data/McKesson/model.bpmn";

    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
    private static final char SEPARATOR = ',';

    private static final String CASE_ID_NAME = "CaseId";
    private static final String TIMESTAMP_NAME = "Timestamp";
    private static final String ACTIVITY_NAME = "EventName";

    private static final String FILE_PATH = "/home/jonas/Data/McKesson/case_joined.arff";
    private static final String TARGET_VARIABLE = "duration";
    private static final String GRAPH_OUTPUT_FILE = "/home/jonas/Data/McKesson/tree.gv";

    public static void main(String[] args) {
        var analysisResults = new ModelAnalyzer().analyzeModel(MODEL_FILE);
        System.out.println(analysisResults);

        var csvLogReader = new CsvLogReader()
                .separator(SEPARATOR)
                .dateFormat(DATE_FORMAT)
                .caseIdName(CASE_ID_NAME)
                .timestampName(TIMESTAMP_NAME)
                .activityName(ACTIVITY_NAME);


//        eventLogTransformation(csvLogReader, "/home/jonas/Data/McKesson/event_sorted.csv");
        simpleArffConversion(csvLogReader, "/home/jonas/Data/McKesson/case_joined.csv");
    }

    private static void eventLogTransformation(CsvLogReader csvLogReader, String fileName) {
        var eventLog = runTimed(() -> new CsvEventLogReader(csvLogReader).read(new File(fileName)), "Reading event log");

//        var endActivityNames = runTimed(() -> extractEndActivityNames(eventLog), "Collecting end activities");
        var transformer = new LogTransformer(eventLog)
                .with(new CaseDurationTransformation())
//                .with(new ActivityExecutionTransformation(endActivityNames))
                .with(new ParallelCaseCountTransformation());
        var transformedLog = runTimed(transformer::transform, "Transforming attributes");

        runTimed(() -> new CsvLogWriter().writeToFile(transformedLog, fileName.replace("event_sorted", "case")), "Writing CSV file");


        var data = runTimed(() -> new DataLoader().loadData(FILE_PATH), "Reading ARFF file");

        var attributeScore = runTimed(() -> new FeatureEvaluator().calculateFeatureScores(data, TARGET_VARIABLE), "Calculating feature scores");

        var importantAttributes = attributeScore.entrySet().stream()
                .filter(entry -> entry.getValue() > 0.2)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        importantAttributes.entrySet().stream()
                .sorted(Comparator.comparingDouble(Map.Entry<String, Double>::getValue).reversed())
                .forEach(entry -> System.out.println(entry.getKey() + " -> " + entry.getValue()));

        new AttributeFilter().filterImportantAttributes(data, importantAttributes.keySet());

        var decisionTreeGraph = runTimed(() -> new DecisionTreeClassifier().buildDecisionRules(data), "Learning decision tree");

        try (var fileWriter = new FileWriter(GRAPH_OUTPUT_FILE)) {
            fileWriter.write(decisionTreeGraph);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void simpleArffConversion(CsvLogReader csvLogReader, String fileName) {
        var caseLog = runTimed(() -> new CsvCaseLogReader(csvLogReader).read(new File(fileName)), "Reading case log");
        runTimed(() -> new ArffLogWriter().writeToFile(caseLog, fileName.replace(".csv", ".arff")), "Writing ARFF file");
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
