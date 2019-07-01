package de.hpi.bpt;

import org.apache.commons.lang3.time.StopWatch;

import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class Main {

    public static final String FILE_PATH = "/home/jonas/Downloads/Hospital_caselog.arff";
    public static final String TARGET_VARIABLE = "duration";

    public static void main(String[] args) {

        var featureEvaluator = new FeatureEvaluator().target(TARGET_VARIABLE);
        var attributeScore = runTimed(() -> featureEvaluator.calculateFeatureScores(FILE_PATH), "Calculating feature scores");

        attributeScore.entrySet().stream()
                .filter(entry -> entry.getValue() > 0.01)
                .sorted(Comparator.comparingDouble(Map.Entry<String, Double>::getValue).reversed())
                .forEach(entry -> System.out.println(entry.getKey() + " -> " + entry.getValue()));
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
