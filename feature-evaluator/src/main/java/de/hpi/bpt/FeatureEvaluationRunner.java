package de.hpi.bpt;

import de.hpi.bpt.evaluation.Clusterer;
import de.hpi.bpt.evaluation.CommonValueCollector;
import de.hpi.bpt.evaluation.CrossValidator;
import de.hpi.bpt.evaluation.FeatureEvaluator;
import de.hpi.bpt.evaluation.decisiontree.DecisionTreeClassifier;
import de.hpi.bpt.util.DataPreprocessor;
import de.hpi.bpt.util.DataSplitter;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import weka.classifiers.Classifier;
import weka.core.Drawable;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static de.hpi.bpt.util.TimeTracker.runTimed;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toSet;

class FeatureEvaluationRunner {

    Map<String, Object> runEvaluation(Instances data, String targetAttribute, String targetValue, List<String> attributesToIgnore, List<String> suspectedDependencies, String preprocessingType) {
        var dataPreprocessor = new DataPreprocessor().ignoring(attributesToIgnore);

        Instances processedData = null;
        if ("nominal_numeric".equals(preprocessingType)) {
            processedData = runTimed(() -> dataPreprocessor.prepareDataNominalKeepingNumeric(data, targetAttribute), "Preparing data");
        } else if ("binary_numeric".equals(preprocessingType)) {
            processedData = runTimed(() -> dataPreprocessor.prepareDataBinaryKeepingNumeric(data, targetAttribute), "Preparing data");
        } else if ("all_nominal".equals(preprocessingType)) {
            processedData = runTimed(() -> dataPreprocessor.prepareDataAllNominal(data, targetAttribute), "Preparing data");
        } else if ("all_binary".equals(preprocessingType)) {
            processedData = runTimed(() -> dataPreprocessor.prepareDataAllBinary(data, targetAttribute), "Preparing data");
        }

        try {
            return runEvaluation(processedData, targetValue, suspectedDependencies);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private Map<String, Object> runEvaluation(Instances data, String targetValue, List<String> suspectedDependencies) throws Exception {
        var featureEvaluator = new FeatureEvaluator();
        var classifier = new DecisionTreeClassifier();
        var commonValueCollector = new CommonValueCollector();
        var clusterer = new Clusterer();
        var validator = new CrossValidator();
        var dataSplitter = new DataSplitter();

        var attributesToKeep = suspectedDependencies.stream().map(attName -> data.attribute(attName).index()).collect(toSet());


        var simpleAttributeSelection = runTimed(() -> featureEvaluator.selectAttributes(data), "Selecting attributes using pearson correlation");
        var directDependencies = featureEvaluator.findDirectDependencies(data, simpleAttributeSelection);
        var highlyCorrelatedAttributes = featureEvaluator.findHighlyCorrelatedAttributes(data, simpleAttributeSelection);
        var reducedData = featureEvaluator.removeNonInterestingAttributes(data, simpleAttributeSelection, attributesToKeep);

        var dataWithSelectedFeatures = runTimed(() -> featureEvaluator.retainImportantFeatures(reducedData, attributesToKeep), "Calculating feature scores");
        var selectedFeatures = IntStream.range(0, dataWithSelectedFeatures.numAttributes())
                .mapToObj(i -> dataWithSelectedFeatures.attribute(i).name())
                .collect(joining("\n"));

        Classifier tree;
        if (data.classAttribute().isNumeric()) {
            tree = runTimed(() -> classifier.buildREPTree(dataWithSelectedFeatures), "Building REP tree");
        } else {
            tree = runTimed(() -> classifier.buildJ48Tree(dataWithSelectedFeatures), "Building J48 tree");
        }

        var treeImage = getTreeImageTag((Drawable) tree);

        var rules = runTimed(() -> classifier.buildPARTRules(dataWithSelectedFeatures), "Building decision rules");

        var crossValidation = runTimed(() -> validator.validate(tree, dataWithSelectedFeatures), "Cross-validating tree");

        var singleClassData = dataSplitter.removeInstancesWithWrongClass(dataWithSelectedFeatures, targetValue);
        var centroids = runTimed(() -> clusterer.clusterPositiveInstances(singleClassData), "Clustering");
        var commonValues = runTimed(() -> commonValueCollector.collectCommonValues(singleClassData), "Collecting common values");

        String stumps = "";
        for (String attributeName : suspectedDependencies) {
            if (data.attribute(attributeName) == null) {
                continue;
            }
            var remove = new Remove();
            remove.setAttributeIndicesArray(new int[]{data.classIndex(), data.attribute(attributeName).index()});
            remove.setInvertSelection(true);
            remove.setInputFormat(data);
            var stump = classifier.buildStumpForAttribute(Filter.useFilter(data, remove));

            stumps += getTreeImageTag(stump) + "\n";
        }

        return new HashMap<>(Map.of(
                "SELECTED_ATTRIBUTES", selectedFeatures,
                "TREE", treeImage,
                "RULES", rules.toString().isEmpty() ? "NONE" : rules.toString(),
                "EVALUATION", crossValidation.toClassDetailsString(),
                "CLUSTERS", centroids.isEmpty() ? "NONE" : centroids,
                "DIRECT_DEPENDENCIES", directDependencies.isEmpty() ? "NONE" : directDependencies,
                "CORRELATED_ATTRIBUTES", highlyCorrelatedAttributes.isEmpty() ? "NONE" : highlyCorrelatedAttributes,
                "COMMON_VALUES", commonValues.isEmpty() ? "NONE" : commonValues,
                "ASSUMPTION_TREES", stumps
        ));
    }

    private String getTreeImageTag(Drawable tree) throws Exception {
        var treeBase64 = Base64.getEncoder().encodeToString(Graphviz.fromString(tree.graph()).render(Format.SVG).toString().getBytes(StandardCharsets.UTF_8));
        return "<img src=\"data:image/svg+xml;utf8;base64, " + treeBase64 + "\"/>";
    }
}
