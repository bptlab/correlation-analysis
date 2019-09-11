package de.hpi.bpt;

import de.hpi.bpt.evaluation.Clusterer;
import de.hpi.bpt.evaluation.CommonValueCollector;
import de.hpi.bpt.evaluation.CrossValidator;
import de.hpi.bpt.evaluation.FeatureEvaluator;
import de.hpi.bpt.evaluation.decisiontree.DecisionTreeClassifier;
import de.hpi.bpt.util.DataLoader;
import de.hpi.bpt.util.DataSplitter;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
import weka.core.Instances;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static de.hpi.bpt.util.TimeTracker.runTimed;
import static java.util.stream.Collectors.joining;

public class FeatureEvaluationRunner {


    public static final String TARGET_VALUE = "false";
    private static final String TARGET_VARIABLE = "Receive Goods_wasexecuted";
    private static final String PROJECT_FOLDER = "Solvay";
    private static final String DATA_FOLDER = "/home/jonas/Data/";
    public static final String FOLDER_PATH = DATA_FOLDER + PROJECT_FOLDER + "/";

    private static final String GRAPH_OUTPUT_FILE = "tree.png";
    private static final String CASES_FILE = "cases.arff";


    public static void main(String[] args) throws Exception {
        var dataLoader = new DataLoader().ignoring("Receive Goods_snumexecutions");
        var featureEvaluator = new FeatureEvaluator();
        var classifier = new DecisionTreeClassifier();
        var clusterer = new Clusterer();
        var validator = new CrossValidator();
        var commonValueCollector = new CommonValueCollector();
        var dataSplitter = new DataSplitter();

        var initialData = runTimed(() -> dataLoader.prepareDataFromFile(FOLDER_PATH + CASES_FILE, TARGET_VARIABLE), "Preparing data");

        var commonValues = runTimed(() -> commonValueCollector.collectCommonValues(dataSplitter.removeInstancesWithWrongClass(initialData)), "Collecting common values");
        var dataWithCommonValueFeatures = featureEvaluator.retainFeaturesWithCommonValues(initialData, commonValues.stream().mapToInt(CommonValueCollector.CommonValue::getAttributeIndex).toArray());

        Pair<String, Instances> directDependencyResult = runTimed(() -> featureEvaluator.findAndRemoveDirectDependencies(dataWithCommonValueFeatures), "Removing direct dependencies");
        var dataWithoutDirectDependencies = directDependencyResult.getRight();

        var dataWithSelectedFeatures = runTimed(() -> featureEvaluator.retainImportantFeatures(dataWithoutDirectDependencies), "Calculating feature scores");

        var tree = runTimed(() -> classifier.buildJ48Tree(dataWithSelectedFeatures), "Building decision tree");
        Graphviz.fromString(tree.graph()).render(Format.PNG).toFile(new File(FOLDER_PATH + GRAPH_OUTPUT_FILE));

        var rules = runTimed(() -> classifier.buildPARTRules(dataWithSelectedFeatures), "Building positive decision rules");
        var positiveRules = Arrays.stream(rules.toString().split("\n\n")).filter(string -> !string.startsWith(":") && string.contains(": " + TARGET_VALUE + " ("))
                .collect(joining("\n\n"));

        var evaluation = runTimed(() -> validator.validate(tree, dataWithSelectedFeatures), "Cross-validating");

        var centroids = "";
        if (evaluation.truePositiveRate(dataWithSelectedFeatures.classAttribute().indexOfValue(TARGET_VALUE)) <= 1) { // TODO ratio
            centroids = runTimed(() -> clusterer.clusterPositiveInstances(dataSplitter.removeInstancesWithWrongClass(dataWithSelectedFeatures)), "Clustering");
        }

        writeAll(
                positiveRules,
                evaluation.toClassDetailsString(),
                commonValues.stream().map(CommonValueCollector.CommonValue::toString).collect(joining("\n")),
                centroids,
                directDependencyResult.getLeft()
        );

    }


    public static void writeToFile(String result, String filePath) throws IOException {
        try (var fileWriter = new FileWriter(filePath)) {
            fileWriter.write(result);
        }
    }

    private static void writeAll(
            String rules,
            String evaluation,
            String commonValues,
            String clusters,
            String directDependencies
    ) throws IOException, URISyntaxException {
        var template = FileUtils.readFileToString(new File(FeatureEvaluationRunner.class.getResource("result.tpl").toURI()), StandardCharsets.UTF_8);
        var result = template
                .replace("{{TARGET_VARIABLE}}", TARGET_VARIABLE)
                .replace("{{FOLDER}}", FOLDER_PATH)
                .replace("{{RULES}}", rules)
                .replace("{{EVALUATION}}", evaluation)
                .replace("{{COMMON_VALUES}}", commonValues)
                .replace("{{CLUSTERS}}", clusters)
                .replace("{{DIRECT_DEPENDENCIES}}", directDependencies);
        writeToFile(result, FOLDER_PATH + "result.html");
    }
}
