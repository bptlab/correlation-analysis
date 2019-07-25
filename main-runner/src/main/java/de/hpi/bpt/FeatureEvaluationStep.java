package de.hpi.bpt;

import weka.core.Instances;

import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;

class FeatureEvaluationStep {
    static void evaluateFeatures(Instances data) {
        var attributeScore = TimeTracker.runTimed(() -> new FeatureEvaluator().calculateFeatureScores(data), "Calculating feature scores");

        var importantAttributes = attributeScore.entrySet().stream()
                .filter(entry -> !entry.getKey().equals(Parameters.CASE_ID_NAME))
                .filter(entry -> entry.getValue() > 0)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        importantAttributes.entrySet().stream()
                .sorted(Comparator.comparingDouble(Map.Entry<String, Double>::getValue).reversed())
                .forEach(entry -> System.out.println(entry.getKey() + " -> " + entry.getValue()));

        new AttributeFilter().filterImportantAttributes(data, importantAttributes.keySet());
    }
}
