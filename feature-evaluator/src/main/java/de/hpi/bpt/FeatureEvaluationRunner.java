package de.hpi.bpt;

import de.hpi.bpt.evaluation.*;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import org.apache.commons.lang3.tuple.Pair;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;
import weka.filters.unsupervised.attribute.StringToNominal;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class FeatureEvaluationRunner {

    private static final String FOLDER = "/home/jonas/Data/BPIC2019/";
    private static final String CASES_FILE = "cases.arff";
    private static final String GRAPH_OUTPUT_FILE = "tree.png";

    private static final String TARGET_VARIABLE = "last_event";
    private static final String TARGET_VALUE = "true";


    public static void main(String[] args) {

        var data = retrieveData();

        //        var result = new FeatureValueToClassRatioCalculator().calculate(data);
//        result.stream().filter(FeatureToClassRatio::hasNotableSplit)
//                .forEach(r -> System.out.println(r + "\n=======\n"));
//        var singleClassData = WekaPreparationStep.removeWithWrongClassValue(data);

        var relevantFeatures = evaluateFeatures(data);
        var relevantData = new AttributeFilter().filterImportantAttributesKeepingClass(data, relevantFeatures);

        buildDecisionTree(data); // relevantData

    }

    private static void buildDecisionTree(Instances data) {
        try {
            var decisionTreeOutput = TimeTracker.runTimed(() -> new DecisionTreeClassifier().buildDecisionRules(data), "Learning decision tree");
            Graphviz.fromString(decisionTreeOutput).render(Format.PNG).toFile(new File(FOLDER + GRAPH_OUTPUT_FILE));
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

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

    private static Instances retrieveData() {
        try {
            var caseLogData = TimeTracker.runTimed(() -> new DataLoader()
                    .loadDataFromFile(FOLDER + CASES_FILE), "Reading ARFF file into Instances");

            var preprocessedData = applyFilters(caseLogData);

            preprocessedData.setClass(preprocessedData.attribute(TARGET_VARIABLE));
            preprocessedData.deleteWithMissingClass();
            return preprocessedData;

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }

    }

    public static Instances removeWithWrongClassValue(Instances data) {
        var newData = new Instances(data);
        newData.setClass(data.classAttribute());

        var classIndex = newData.classIndex();
        var targetValueIndex = newData.instance(0).attribute(classIndex).indexOfValue(TARGET_VALUE);

        newData.removeIf(instance -> !(instance.value(classIndex) == targetValueIndex));
        return newData;
    }

    private static Instances applyFilters(Instances data) throws Exception {
        var nominal = Filter.useFilter(data, stringToNominalFilter(data));
        return Filter.useFilter(nominal, removeEmptyAttributesFilter(nominal));
    }

    private static StringToNominal stringToNominalFilter(Instances data) throws Exception {
        var stringToNominal = new StringToNominal();
        stringToNominal.setAttributeRange("first-last");
        stringToNominal.setInputFormat(data);
        return stringToNominal;
    }

    private static Remove removeEmptyAttributesFilter(final Instances data) throws Exception {
        var toRemove = IntStream.range(0, data.numAttributes())
                .filter(i -> {
                    var attribute = data.attribute(i);
                    return attribute.isNominal() && attribute.numValues() == 0;
                })
                .toArray();
        var remove = new Remove();
        remove.setAttributeIndicesArray(toRemove);
        remove.setInputFormat(data);
        return remove;
    }
}
