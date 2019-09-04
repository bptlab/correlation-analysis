package de.hpi.bpt;

import de.hpi.bpt.evaluation.Clusterer;
import de.hpi.bpt.evaluation.CommonValueCollector;
import de.hpi.bpt.evaluation.FeatureEvaluator;
import de.hpi.bpt.evaluation.FeatureValueToClassRatioCalculator;
import de.hpi.bpt.evaluation.FeatureValueToClassRatioCalculator.FeatureToClassRatio;
import de.hpi.bpt.evaluation.decisiontree.DecisionTreeClassifier;
import de.hpi.bpt.util.DataLoader;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import weka.core.Instances;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

import static de.hpi.bpt.util.TimeTracker.runTimed;
import static java.util.stream.Collectors.joining;

public class FeatureEvaluationRunner {

    public static final String DIRECT_DEPENDENCIES_OUTPUT_FILE = "directdependencies.txt";
    public static final String TARGET_VALUE = "true";
    private static final String PROJECT_FOLDER = "BPIC2018";
    private static final String CASES_FILE = "cases.arff";
    private static final String GRAPH_OUTPUT_FILE = "tree.png";
    private static final String DATA_FOLDER = "/home/jonas/Data/";
    public static final String FOLDER_PATH = DATA_FOLDER + PROJECT_FOLDER + "/";
    private static final String RULES_OUTPUT_FILE = "rules.txt";
    private static final String CLUSTERS_OUTPUT_FILE = "clusters.txt";
    private static final String TARGET_VARIABLE = "reopened";

    public static void main(String[] args) throws Exception {
        var dataLoader = new DataLoader();
        var featureEvaluator = new FeatureEvaluator();
        var classifier = new DecisionTreeClassifier();
        var clusterer = new Clusterer();


        var data = runTimed(() -> dataLoader.prepareDataFromFile(FOLDER_PATH + CASES_FILE, TARGET_VARIABLE), "Preparing data");
        var data2 = runTimed(() -> featureEvaluator.findAndRemoveDirectDependencies(data), "Removing direct dependencies");
        var data3 = runTimed(() -> featureEvaluator.retainImportantFeatures(data2), "Calculating feature scores");

        var tree = runTimed(() -> classifier.buildJ48Tree(data3), "Building decision tree");
        Graphviz.fromString(tree.graph()).render(Format.PNG).toFile(new File(FOLDER_PATH + GRAPH_OUTPUT_FILE));

        var rules = runTimed(() -> classifier.buildPARTRules(data3), "Building positive decision rules");
        var positiveRules = Arrays.stream(rules.toString().split("\n\n")).filter(string -> !string.startsWith(":") && string.contains(": " + TARGET_VALUE + " ("))
                .collect(joining("\n=\n"));
        writeToFile(positiveRules, FOLDER_PATH + RULES_OUTPUT_FILE);

        var centroids = runTimed(() -> clusterer.clusterPositiveInstances(tree, data3), "Clustering");
        writeToFile(centroids, FeatureEvaluationRunner.FOLDER_PATH + FeatureEvaluationRunner.CLUSTERS_OUTPUT_FILE);
//        writeToFile(new RegressionAnalysis().buildLogisticClassifier(data3), FOLDER + "logit.pmml");

        Runtime.getRuntime().exec(DATA_FOLDER + "render.sh " + PROJECT_FOLDER);
    }

    private static void printFeatureToClassRatio(Instances data) {
        var result = new FeatureValueToClassRatioCalculator().calculate(data);
        result.stream().filter(FeatureToClassRatio::hasNotableSplit)
                .forEach(r -> System.out.println(r + "\n=======\n"));
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


    public static void writeToFile(String result, String filePath) {
        try (var fileWriter = new FileWriter(filePath)) {
            fileWriter.write(result);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
