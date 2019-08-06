package de.hpi.bpt;

import org.apache.commons.lang3.tuple.Pair;
import weka.core.Instances;

import java.util.*;
import java.util.stream.Collectors;

class FeatureEvaluationStep {
    static Set<Integer> evaluateFeatures(Instances data) {
        var attributeScore = TimeTracker.runTimed(() -> new FeatureEvaluator().calculateFeatureScores(data), "Calculating feature scores");

        var importantAttributes = attributeScore.entrySet().stream()
                .filter(entry -> entry.getValue() > 0.05)
//                .filter(entry -> entry.getValue() > -0.2)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        importantAttributes.entrySet().stream()
                .map(entry -> Pair.of(data.attribute(entry.getKey()).name(), entry.getValue()))
//                .sorted(Comparator.comparingDouble(Map.Entry<String, Double>::getValue).reversed())
                .sorted(Comparator.comparing(Pair<String, Double>::getKey))
                .forEach(pair -> System.out.println(pair.getKey() + " -> " + pair.getValue()));

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

        var scanner = new Scanner(System.in);
        System.out.println("Remove features containing strings (comma separated):");
        var toRemove = scanner.nextLine().split(",");
        scanner.close();
        var indices = new HashSet<>(importantAttributes.keySet());
        indices.removeIf(i -> {
            for (String s : toRemove) {
                if (data.attribute(i).name().contains(s)) {
                    return true;
                }
            }
            return false;
        });
        return indices;
    }
}
