package de.hpi.bpt;

import weka.core.Instances;

import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;

import static de.hpi.bpt.Parameters.TARGET_VARIABLE;

class FeatureEvaluationStep {
    static void evaluateFeatures(Instances data) {
        var attributeScore = TimeTracker.runTimed(() -> new FeatureEvaluator().calculateFeatureScores(data), "Calculating feature scores");

        var importantAttributes = attributeScore.entrySet().stream()
//                .filter(entry -> entry.getValue() > 0.005)
                .filter(entry -> !entry.getKey().equals(TARGET_VARIABLE))
                .filter(entry -> entry.getValue() > -0.2)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        importantAttributes.entrySet().stream()
//                .sorted(Comparator.comparingDouble(Map.Entry<String, Double>::getValue).reversed())
                .sorted(Comparator.comparing(Map.Entry<String, Double>::getKey))
                .forEach(entry -> System.out.println(entry.getKey() + " -> " + entry.getValue()));

        var filteredData = new AttributeFilter().filterImportantAttributes(data, importantAttributes.keySet());
        var commonValues = new CommonValueCollector().collectCommonValues(filteredData);

        var numAttributes = data.size();
        commonValues.stream()
                .sorted(Comparator.comparing(CommonValueCollector.CommonValue::getAttributeName))
                .forEach(commonValue -> System.out.println(
                        String.format(
                                "'%s': %s (%d/%d)",
                                commonValue.getAttributeName(),
                                commonValue.getValue(),
                                commonValue.getOccurrences(),
                                numAttributes
                        )));
    }
}
