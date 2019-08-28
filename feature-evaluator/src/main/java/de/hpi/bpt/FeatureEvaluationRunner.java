package de.hpi.bpt;

import de.hpi.bpt.evaluation.CommonValueCollector;
import de.hpi.bpt.evaluation.decisiontree.DecisionTreeClassifier;
import de.hpi.bpt.evaluation.FeatureEvaluator;
import de.hpi.bpt.evaluation.FeatureValueToClassRatioCalculator;
import de.hpi.bpt.evaluation.FeatureValueToClassRatioCalculator.FeatureToClassRatio;
import de.hpi.bpt.evaluation.regression.RegressionAnalysis;
import de.hpi.bpt.util.CaseFilter;
import de.hpi.bpt.util.DataLoader;
import de.hpi.bpt.util.TimeTracker;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import org.apache.commons.lang3.tuple.Pair;
import weka.core.Instances;
import weka.core.pmml.jaxbbindings.Regression;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class FeatureEvaluationRunner {

    private static final String FOLDER = "/home/jonas/Data/BPIC2019/";
    private static final String CASES_FILE = "cases.arff";
    private static final String GRAPH_OUTPUT_FILE = "tree.png";

    private static final String TARGET_VARIABLE = "name_end";
    private static final String TARGET_VALUE = "true";


    public static void main(String[] args) {
        var caseFilter = new CaseFilter();
        var dataLoader = new DataLoader();

        var data = dataLoader.prepareDataFromFile(FOLDER + CASES_FILE, TARGET_VARIABLE);
//        var singleClassData = caseFilter.removeWithWrongClassValue(data, TARGET_VALUE);
//
//
//        var relevantFeatureIndices = reliefF(data);
//        var relevantData = caseFilter.filterImportantAttributesKeepingClass(data, relevantFeatureIndices);
//
//        printCommonValues(relevantData);
//        printFeatureToClassRatio(relevantData);
//
//        buildDecisionTrees(data);

        writeToFile(new RegressionAnalysis().buildLogisticClassifier(data), FOLDER + "logit.pmml");
    }

    private static void printFeatureToClassRatio(Instances data) {
        var result = new FeatureValueToClassRatioCalculator().calculate(data);
        result.stream().filter(FeatureToClassRatio::hasNotableSplit)
                .forEach(r -> System.out.println(r + "\n=======\n"));
    }

    private static void buildDecisionTrees(Instances data) {
        try {
            var decisionTreeOutput = TimeTracker.runTimed(() -> new DecisionTreeClassifier().buildJ48Tree(data), "Learning J48 tree");
            Graphviz.fromString(decisionTreeOutput).render(Format.PNG).toFile(new File(FOLDER + GRAPH_OUTPUT_FILE));


        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    static Set<Integer> reliefF(Instances data) {
        var attributeScore = TimeTracker.runTimed(() -> new FeatureEvaluator().calculateFeatureScores(data), "Calculating feature scores");

        var importantAttributes = attributeScore.entrySet().stream()
                .filter(entry -> entry.getValue() > 0.05)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        importantAttributes.entrySet().stream()
                .map(entry -> Pair.of(data.attribute(entry.getKey()).name(), entry.getValue()))
                .sorted(Comparator.comparing(Pair<String, Double>::getKey))
                .forEach(pair -> System.out.println(pair.getKey() + " -> " + pair.getValue()));

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

    private static void printCommonValues(Instances data) {
        var commonValues = new CommonValueCollector().collectCommonValues(data);

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


    private static void writeToFile(String result, String filePath) {
        try (var fileWriter = new FileWriter(filePath)) {
            fileWriter.write(result);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
