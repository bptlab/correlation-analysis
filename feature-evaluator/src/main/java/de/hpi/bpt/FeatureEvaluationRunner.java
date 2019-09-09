package de.hpi.bpt;

import de.hpi.bpt.evaluation.Clusterer;
import de.hpi.bpt.evaluation.CrossValidator;
import de.hpi.bpt.evaluation.FeatureEvaluator;
import de.hpi.bpt.evaluation.decisiontree.DecisionTreeClassifier;
import de.hpi.bpt.util.DataLoader;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

import static de.hpi.bpt.util.TimeTracker.runTimed;
import static java.util.stream.Collectors.joining;

public class FeatureEvaluationRunner {


    private static final String TARGET_VARIABLE = "Critical";
    public static final String TARGET_VALUE = "Yes";

    private static final String PROJECT_FOLDER = "Solvay";
    private static final String DATA_FOLDER = "/home/jonas/Data/";
    public static final String FOLDER_PATH = DATA_FOLDER + PROJECT_FOLDER + "/";

    public static final String DIRECT_DEPENDENCIES_OUTPUT_FILE = "directdependencies.txt";
    private static final String GRAPH_OUTPUT_FILE = "tree.png";
    private static final String CASES_FILE = "cases.arff";
    private static final String RULES_OUTPUT_FILE = "rules.txt";
    private static final String CLUSTERS_OUTPUT_FILE = "clusters.txt";
    private static final String EVALUATION_FILE = "evaluation.txt";


    public static void main(String[] args) throws Exception {
        var dataLoader = new DataLoader().ignoring("Criticality");
        var featureEvaluator = new FeatureEvaluator();
        var classifier = new DecisionTreeClassifier();
        var clusterer = new Clusterer();
        var validator = new CrossValidator();

        var data = runTimed(() -> dataLoader.prepareDataFromFile(FOLDER_PATH + CASES_FILE, TARGET_VARIABLE), "Preparing data");
        var data2 = runTimed(() -> featureEvaluator.findAndRemoveDirectDependencies(data), "Removing direct dependencies");
        var data3 = runTimed(() -> featureEvaluator.retainImportantFeatures(data2), "Calculating feature scores");

        var tree = runTimed(() -> classifier.buildJ48Tree(data3), "Building decision tree");
        Graphviz.fromString(tree.graph()).render(Format.PNG).toFile(new File(FOLDER_PATH + GRAPH_OUTPUT_FILE));

        var rules = runTimed(() -> classifier.buildPARTRules(data3), "Building positive decision rules");
        var positiveRules = Arrays.stream(rules.toString().split("\n\n")).filter(string -> !string.startsWith(":") && string.contains(": " + TARGET_VALUE + " ("))
                .collect(joining("\n\n"));
        writeToFile(positiveRules, FOLDER_PATH + RULES_OUTPUT_FILE);

        var evaluation = runTimed(() -> validator.validate(tree, data), "Cross-validating");
        writeToFile(evaluation.toClassDetailsString(), FOLDER_PATH + EVALUATION_FILE);

        var centroids = "";
        if (evaluation.truePositiveRate(data3.classAttribute().indexOfValue(TARGET_VALUE)) <= 1) { // TODO ratio
            centroids = runTimed(() -> clusterer.clusterPositiveInstances(data3), "Clustering");
        }
        writeToFile(centroids, FOLDER_PATH + CLUSTERS_OUTPUT_FILE);

//        Runtime.getRuntime().exec(DATA_FOLDER + "render.sh " + PROJECT_FOLDER);
    }


    public static void writeToFile(String result, String filePath) {
        try {
            var fileWriter = new FileWriter(filePath);
            fileWriter.write(result);
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
