package de.hpi.bpt;

import de.hpi.bpt.evaluation.CrossValidator;
import de.hpi.bpt.evaluation.FeatureEvaluator;
import de.hpi.bpt.evaluation.decisiontree.DecisionRulesClassifier;
import de.hpi.bpt.evaluation.decisiontree.DecisionTreeClassifier;
import de.hpi.bpt.util.DataPreprocessor;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import weka.classifiers.Classifier;
import weka.core.Drawable;
import weka.core.Instances;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static de.hpi.bpt.util.TimeTracker.runTimed;
import static java.util.stream.Collectors.joining;

class FeatureEvaluationRunner {

    private final DataPreprocessor dataPreprocessor = new DataPreprocessor();
    private final FeatureEvaluator featureEvaluator = new FeatureEvaluator();
    private final DecisionTreeClassifier treeClassifier = new DecisionTreeClassifier();
    private final DecisionRulesClassifier rulesClassifier = new DecisionRulesClassifier();
    private final CrossValidator validator = new CrossValidator();
    private Instances processedData;
    private String projectName;
    private String targetAttribute;
    private String stumps;
    private Set<String> ignoredAttributes = new HashSet<>();
    private Set<String> suspectedDependencies = new HashSet<>();

    FeatureEvaluationRunner projectName(String projectName) {
        this.projectName = projectName;
        return this;
    }

    FeatureEvaluationRunner targetAttribute(String targetAttribute) {
        this.targetAttribute = targetAttribute;
        return this;
    }

    FeatureEvaluationRunner suspectedDependencies(List<String> suspectedDependencies) {
        this.suspectedDependencies.addAll(suspectedDependencies);
        return this;
    }

    void reset() {
        ignoredAttributes.clear();
        suspectedDependencies.clear();
    }

    Map<String, Object> runFirstEvaluation(Instances data) {
        try {
            return doRunFirstEvaluation(data);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    Map<String, Object> runSubsequentEvaluation(List<String> newlyIgnoredAttributes) {
        try {
            return doRunSubsequentEvaluation(newlyIgnoredAttributes);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private Map<String, Object> doRunFirstEvaluation(Instances data) throws Exception {
        data.setClass(data.attribute(targetAttribute));

        var preprocessedData = runTimed(() -> dataPreprocessor.simplePreprocess(data), "Preparing data");
        var attributeSelection = runTimed(() -> featureEvaluator.selectAttributes(preprocessedData), "Selecting attributes");
        var processedData = featureEvaluator.retainTop50Attributes(preprocessedData, attributeSelection, suspectedDependencies);
        var directDependencies = featureEvaluator.findDirectDependencies(data, attributeSelection);
        var highlyCorrelatedAttributes = featureEvaluator.findHighlyCorrelatedAttributes(data, attributeSelection);
        var reducedData = featureEvaluator.removeNonInterestingAttributes(processedData, attributeSelection, suspectedDependencies);
        this.processedData = dataPreprocessor.replaceMissingStringValuesWithConstant(reducedData);

        var dataWithSelectedFeatures = runTimed(() -> featureEvaluator.retainImportantFeatures(processedData, suspectedDependencies), "Calculating feature scores");
        var selectedFeatures = IntStream.range(0, dataWithSelectedFeatures.numAttributes())
                .mapToObj(i -> dataWithSelectedFeatures.attribute(i).name())
                .collect(joining("\n"));

        Classifier tree = runTimed(() -> treeClassifier.buildJ48Tree(dataWithSelectedFeatures), "Building J48 tree");
        var treeImage = getTreeImageTag((Drawable) tree);

        var rules = runTimed(() -> rulesClassifier.buildPARTRules(dataWithSelectedFeatures), "Building decision rules");

//        var crossValidation = runTimed(() -> validator.validate(rules, dataWithSelectedFeatures), "Cross-validating tree");

        stumps = runTimed(() -> treeClassifier.buildStumpsForAttributes(dataWithSelectedFeatures, suspectedDependencies), "Checking suspected dependencies");

        var result = getFixedValues();
        result.put("SELECTED_ATTRIBUTES", selectedFeatures);
        result.put("TREE", treeImage);
        result.put("RULES", rules.toString());
//        result.put("EVALUATION", crossValidation.toClassDetailsString());
        if (!directDependencies.isEmpty()) {
            result.put("DIRECT_DEPENDENCIES", directDependencies);
        }
        if (!highlyCorrelatedAttributes.isEmpty()) {
            result.put("CORRELATED_ATTRIBUTES", highlyCorrelatedAttributes);
        }
        return result;
    }


    private Map<String, Object> doRunSubsequentEvaluation(List<String> newlyIgnoredAttributes) throws Exception {
        ignoredAttributes.addAll(newlyIgnoredAttributes);
        processedData = dataPreprocessor.remove(newlyIgnoredAttributes, processedData);


        var simpleAttributeSelection = runTimed(() -> featureEvaluator.selectAttributes(processedData), "Selecting attributes");
        var directDependencies = featureEvaluator.findDirectDependencies(processedData, simpleAttributeSelection);
        var highlyCorrelatedAttributes = featureEvaluator.findHighlyCorrelatedAttributes(processedData, simpleAttributeSelection);
        var reducedData = featureEvaluator.removeNonInterestingAttributes(processedData, simpleAttributeSelection, suspectedDependencies);

        var dataWithSelectedFeatures = runTimed(() -> featureEvaluator.retainImportantFeatures(reducedData, suspectedDependencies), "Calculating feature scores");
        var selectedFeatures = IntStream.range(0, dataWithSelectedFeatures.numAttributes())
                .mapToObj(i -> dataWithSelectedFeatures.attribute(i).name())
                .collect(joining("\n"));

        Classifier tree = runTimed(() -> treeClassifier.buildJ48Tree(dataWithSelectedFeatures), "Building J48 tree");
        var treeImage = getTreeImageTag((Drawable) tree);

        var rules = runTimed(() -> rulesClassifier.buildPARTRules(dataWithSelectedFeatures), "Building decision rules");

//        var crossValidation = runTimed(() -> validator.validate(rules, dataWithSelectedFeatures), "Cross-validating tree");

        var result = getFixedValues();
        result.put("SELECTED_ATTRIBUTES", selectedFeatures);
        result.put("TREE", treeImage);
        result.put("RULES", rules.toString());
//        result.put("EVALUATION", crossValidation.toClassDetailsString());
        if (!directDependencies.isEmpty()) {
            result.put("DIRECT_DEPENDENCIES", directDependencies);
        }
        if (!highlyCorrelatedAttributes.isEmpty()) {
            result.put("CORRELATED_ATTRIBUTES", highlyCorrelatedAttributes);
        }
        return result;
    }

    private String getTreeImageTag(Drawable tree) throws Exception {
        var treeBase64 = Base64.getEncoder().encodeToString(Graphviz.fromString(tree.graph()).render(Format.SVG).toString().getBytes(StandardCharsets.UTF_8));
        return "<img src=\"data:image/svg+xml;utf8;base64, " + treeBase64 + "\"/>";
    }

    private Map<String, Object> getFixedValues() {
        var result = new HashMap<String, Object>();
        result.put("projectName", projectName);
        result.put("targetAttribute", targetAttribute);
        result.put("attributes", getAttributesSorted());
        if (!ignoredAttributes.isEmpty()) {
            result.put("ignoredAttributes", ignoredAttributes);
        }
        if (!suspectedDependencies.isEmpty()) {
            result.put("suspectedDependencies", suspectedDependencies);
            result.put("assumptionStumps", stumps);
        }

        return result;
    }

    private List<String> getAttributesSorted() {
        return IntStream.range(0, processedData.numAttributes())
                .mapToObj(i -> processedData.attribute(i).name())
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .collect(Collectors.toList());
    }
}
