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
import weka.classifiers.Classifier;
import weka.core.Drawable;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.stream.IntStream;

import static de.hpi.bpt.util.TimeTracker.runTimed;
import static java.util.stream.Collectors.joining;

public class FeatureEvaluationRunner {

    public static final Project PROJECT = Project.SIGNAVIO_SALESFORCE_OPPS;

    private static final String DATA_FOLDER = "/home/jonas/Data/";
    private static final String FOLDER_PATH = DATA_FOLDER + PROJECT.getFolderName() + "/";
    private static final String TREE_GRAPH_OUTPUT_FILE = "tree.png";
    private static final String CASES_FILE = "cases.arff";


    public static void main(String[] args) throws Exception {
        var dataLoader = new DataLoader().ignoring(
                "OppProbability", "OppStageName", // Direct
                " Opportunity closed (won)_snumexecutions", "OppExpectedNetNewARR", //, // High correlation
                "name_end", "OppLossReason",
                " Customer contact role added: Billing Contact", " Customer contact role added: Tenant Owner",
                " Opportunity stage set to 6 (Finalizing Closure)_snumexecutions", " Opportunity stage set to 7 (Pending)_snumexecutions",
                "Opportunity Dead", "Opportunity closed (lost)"
//                "_snumexecutions"
        );
        var nominalWithNumericData = runTimed(() -> dataLoader.prepareDataFromFileNominalKeepingNumeric(FOLDER_PATH + CASES_FILE, PROJECT.getTargetVariable()), "Preparing data");
        var nominalData = runTimed(() -> dataLoader.prepareDataFromFileNominal(FOLDER_PATH + CASES_FILE, PROJECT.getTargetVariable()), "Preparing data");
        var binaryWithNumericData = runTimed(() -> dataLoader.prepareDataFromFileBinaryKeepingNumeric(FOLDER_PATH + CASES_FILE, PROJECT.getTargetVariable()), "Preparing data");
        var binaryData = runTimed(() -> dataLoader.prepareDataFromFileAllBinary(FOLDER_PATH + CASES_FILE, PROJECT.getTargetVariable()), "Preparing data");

        runEvaluation(nominalWithNumericData, "nominal_with_numeric");
        runEvaluation(nominalData, "all_nominal");
        runEvaluation(binaryWithNumericData, "binary_with_numeric");
        runEvaluation(binaryData, "all_binary");
    }

    private static void runEvaluation(Instances data, String suffix) throws Exception {
        var featureEvaluator = new FeatureEvaluator();
        var classifier = new DecisionTreeClassifier();
        var commonValueCollector = new CommonValueCollector();
        var clusterer = new Clusterer();
        var validator = new CrossValidator();
        var dataSplitter = new DataSplitter();

        var simpleAttributeSelection = runTimed(() -> featureEvaluator.selectAttributes(data), "Selecting attributes using symmetric uncertainty");
        var directDependencies = featureEvaluator.findDirectDependencies(data, simpleAttributeSelection);
        var highlyCorrelatedAttributes = featureEvaluator.findHighlyCorrelatedAttributes(data, simpleAttributeSelection);
        var dataWithoutDirectDependencies = featureEvaluator.removeNonInterestingAttributes(data, simpleAttributeSelection);

        var dataWithSelectedFeatures = runTimed(() -> featureEvaluator.retainImportantFeatures(dataWithoutDirectDependencies), "Calculating feature scores");
        var selectedFeatures = IntStream.range(0, dataWithSelectedFeatures.numAttributes())
                .mapToObj(i -> dataWithSelectedFeatures.attribute(i).name())
                .collect(joining("\n"));

        Classifier tree;
        if (data.classAttribute().isNumeric()) {
            tree = runTimed(() -> classifier.buildREPTree(dataWithSelectedFeatures), "Building REP tree");
        } else {
            tree = runTimed(() -> classifier.buildJ48Tree(dataWithSelectedFeatures), "Building J48 tree");
        }

        Graphviz.fromString(((Drawable) tree).graph()).render(Format.PNG).toFile(new File(FOLDER_PATH + suffix + TREE_GRAPH_OUTPUT_FILE));

        var rules = runTimed(() -> classifier.buildPARTRules(dataWithSelectedFeatures), "Building positive decision rules");
        var positiveRules = Arrays.stream(rules.toString().split("\n\n"))
                .filter(string -> !string.startsWith(":") && string.contains(": " + PROJECT.getTargetValue() + " ("))
                .map(rule ->
                        Arrays.stream(rule.split("\n"))
                                .map(line -> line.contains(" = t") ? "<strong>" + line + "</strong>" : line)
                                .collect(joining("\n"))
                )
                .collect(joining("\n\n"));

        var crossValidation = runTimed(() -> validator.validate(tree, dataWithSelectedFeatures), "Cross-validating tree");

        var singleClassData = dataSplitter.removeInstancesWithWrongClass(dataWithSelectedFeatures);
        var centroids = runTimed(() -> clusterer.clusterPositiveInstances(singleClassData), "Clustering");
        var commonValues = runTimed(() -> commonValueCollector.collectCommonValues(singleClassData), "Collecting common values");

        String stumps = "";
        for (String attributeName : PROJECT.getAssumedCorrelations()) {
            if (data.attribute(attributeName) == null) {
                continue;
            }
            var remove = new Remove();
            remove.setAttributeIndicesArray(new int[]{data.classIndex(), data.attribute(attributeName).index()});
            remove.setInvertSelection(true);
            remove.setInputFormat(data);
            var stump = classifier.buildStumpForAttribute(Filter.useFilter(data, remove));

            var treeFileName = FOLDER_PATH + attributeName + TREE_GRAPH_OUTPUT_FILE;
            Graphviz.fromString(stump.graph()).render(Format.PNG).toFile(new File(treeFileName));
            stumps += "<img src=\"" + treeFileName + "\" />\n";
        }

        writeAll(
                suffix,
                selectedFeatures,
                rules.toString(),//positiveRules,
                crossValidation.toClassDetailsString(),
                centroids,
                directDependencies,
                highlyCorrelatedAttributes,
                commonValues,
                stumps
        );
    }


    private static void writeAll(
            String suffix,
            String selectedAttributes,
            String rules,
            String evaluation,
            String clusters,
            String directDependencies,
            String highlyCorrelatedAttributes,
            String commonValues,
            String stumps
    ) throws IOException, URISyntaxException {
        var template = FileUtils.readFileToString(new File(FeatureEvaluationRunner.class.getResource("result.tpl").toURI()), StandardCharsets.UTF_8);
        var result = template
                .replace("{{PROJECT}}", suffix + " (" + PROJECT.getFolderName() + ")")
                .replace("{{TARGET_VARIABLE}}", PROJECT.getTargetVariable())
                .replace("{{SELECTED_ATTRIBUTES}}", selectedAttributes)
                .replace("{{FOLDER}}", FOLDER_PATH + suffix)
                .replace("{{RULES}}", rules.isEmpty() ? "NONE" : rules)
                .replace("{{EVALUATION}}", evaluation)
                .replace("{{CLUSTERS}}", clusters.isEmpty() ? "NONE" : clusters)
                .replace("{{DIRECT_DEPENDENCIES}}", directDependencies.isEmpty() ? "NONE" : directDependencies)
                .replace("{{CORRELATED_ATTRIBUTES}}", highlyCorrelatedAttributes.isEmpty() ? "NONE" : highlyCorrelatedAttributes)
                .replace("{{COMMON_VALUES}}", commonValues.isEmpty() ? "NONE" : commonValues)
                .replace("{{ASSUMPTION_TREES}}", stumps.isEmpty() ? "NONE" : stumps);

        try (var fileWriter = new FileWriter(FOLDER_PATH + "result_" + suffix + ".html")) {
            fileWriter.write(result);
        }
    }
}
