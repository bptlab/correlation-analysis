package de.hpi.bpt;

import org.apache.commons.lang3.time.StopWatch;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class Main {

    private static final String FILE_PATH = "/home/jonas/Data/Macif/Demands_Joined.arff";
    private static final String TARGET_VARIABLE = "duration";
    private static final String GRAPH_OUTPUT_FILE = "/home/jonas/Data/Macif/Demands_Result.gv";

    public static void main(String[] args) {

        var data = runTimed(() -> new DataLoader().loadData(FILE_PATH), "Reading ARFF file");

        var attributeScore = runTimed(() -> new FeatureEvaluator().calculateFeatureScores(data, TARGET_VARIABLE), "Calculating feature scores");

        var importantAttributes = attributeScore.entrySet().stream()
                .filter(entry -> entry.getValue() > 0.03)
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

    private static <T> T runTimed(Supplier<T> function, String message) {
        StopWatch stopWatch = new StopWatch();
        System.out.println("Starting: " + message);
        stopWatch.start();
        var result = function.get();
        stopWatch.stop();
        System.out.println("Finished: " + message + " (" + stopWatch.getTime(TimeUnit.MILLISECONDS) + "ms)");
        return result;
    }
}
