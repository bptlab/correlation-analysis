package de.hpi.bpt;

import de.hpi.bpt.datastructures.RowCaseLog;
import de.hpi.bpt.feature.AnalysisResult;
import de.hpi.bpt.io.ArffCaseLogWriter;
import de.hpi.bpt.io.CsvCaseLogReader;
import de.hpi.bpt.io.CsvEventLogReader;
import de.hpi.bpt.io.CsvLogReader;
import de.hpi.bpt.transformation.LogTransformer;
import de.hpi.bpt.transformation.time.CaseDurationTransformation;
import de.hpi.bpt.transformation.time.ParallelCaseCountTransformation;
import org.apache.commons.lang3.time.StopWatch;
import weka.core.Instances;

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

    private static final String MODEL_FILE = "/home/jonas/Data/Macif/model.bpmn";
    private static final String EVENTS_FILE = "/home/jonas/Data/Macif/event_sorted.csv";
    private static final String ATTRIBUTES_FILE = "/home/jonas/Data/McKesson/case_attribute.csv";

    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
    private static final char SEPARATOR = ';';

    private static final String CASE_ID_NAME = "CaseId";
    private static final String TIMESTAMP_NAME = "Timestamp";
    private static final String ACTIVITY_NAME = "EventName";

    private static final String TARGET_VARIABLE = "duration";
    private static final String GRAPH_OUTPUT_FILE = "/home/jonas/Data/McKesson/tree.gv";

    public static void main(String[] args) {
        var analysisResults = retrieveAnalysisResults();
        var caseLog = retrieveCaseLog(analysisResults);
        var data = retrieveData(caseLog);

        evaluateFeatures(data);
        buildDecisionTree(data);
    }

    private static void buildDecisionTree(Instances data) {
        var decisionTreeGraph = runTimed(() -> new DecisionTreeClassifier().buildDecisionRules(data), "Learning decision tree");

        try (var fileWriter = new FileWriter(GRAPH_OUTPUT_FILE)) {
            fileWriter.write(decisionTreeGraph);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void evaluateFeatures(Instances data) {
        var attributeScore = runTimed(() -> new FeatureEvaluator().calculateFeatureScores(data, TARGET_VARIABLE), "Calculating feature scores");

        var importantAttributes = attributeScore.entrySet().stream()
                .filter(entry -> entry.getValue() > 0.01)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        importantAttributes.entrySet().stream()
                .sorted(Comparator.comparingDouble(Map.Entry<String, Double>::getValue).reversed())
                .forEach(entry -> System.out.println(entry.getKey() + " -> " + entry.getValue()));

        new AttributeFilter().filterImportantAttributes(data, importantAttributes.keySet());
    }

    private static Instances retrieveData(RowCaseLog caseLog) {
        // TODO can this be done without String serialization?
        var caseLogAsString = new ArffCaseLogWriter().writeToString(caseLog);
        return runTimed(() -> new DataLoader().loadData(caseLogAsString), "Reading ARFF string into Instances");
    }

    private static Set<AnalysisResult> retrieveAnalysisResults() {
        return runTimed(() -> new ModelAnalyzer().analyzeModel(MODEL_FILE), "Analyzing model");
    }

    private static RowCaseLog retrieveCaseLog(Set<AnalysisResult> analysisResults) {
        var csvLogReader = new CsvLogReader()
                .separator(SEPARATOR)
                .dateFormat(DATE_FORMAT)
                .caseIdName(CASE_ID_NAME)
                .timestampName(TIMESTAMP_NAME)
                .activityName(ACTIVITY_NAME);

        var eventLog = runTimed(() -> new CsvEventLogReader(csvLogReader).read(new File(EVENTS_FILE)), "Reading event log");

        var transformer = new LogTransformer(eventLog)
                .with(new CaseDurationTransformation())
//                .with(new ActivityExecutionTransformation(endActivityNames))
                .with(new ParallelCaseCountTransformation())
                .withAnalysisResults(analysisResults);

        var attributesLog = runTimed(() -> new CsvCaseLogReader(csvLogReader).readToRowCaseLog(new File(ATTRIBUTES_FILE), "Attributes"), "Reading attributes log");

        return runTimed(() -> transformer.transformJoining(attributesLog), "Transforming attributes");
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
