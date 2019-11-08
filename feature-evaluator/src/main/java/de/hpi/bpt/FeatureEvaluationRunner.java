package de.hpi.bpt;

import de.hpi.bpt.evaluation.ClassifierValidator;
import de.hpi.bpt.evaluation.FeatureEvaluator;
import de.hpi.bpt.evaluation.decisiontree.DecisionTreeClassifier;
import de.hpi.bpt.evaluation.decisiontree.TraversableJ48;
import de.hpi.bpt.util.DataPreprocessor;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
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
    private final ClassifierValidator validator = new ClassifierValidator();
    private Instances processedData;
    private String projectName;
    private String targetAttribute;
    private Optional<String> targetValue;
    private String stumps;
    private Set<String> ignoredAttributes = new HashSet<>();
    private Set<String> suspectedDependencies = new HashSet<>();
    private String directDependencies;
    private String highlyCorrelatedAttributes;
    private String selectedFeatures;
    private TraversableJ48 tree;
    private String rules;

    FeatureEvaluationRunner projectName(String projectName) {
        this.projectName = projectName;
        return this;
    }

    FeatureEvaluationRunner targetAttribute(String targetAttribute) {
        this.targetAttribute = targetAttribute;
        return this;
    }


    public FeatureEvaluationRunner targetValue(Optional<String> targetValue) {
        this.targetValue = targetValue;
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
        Instances preprocessedData;

        if (targetValue.isPresent()) {
            var targetValueIndex = data.classAttribute().indexOfValue(targetValue.get());
            if (targetValueIndex == -1) {
                throw new RuntimeException("Invalid target value selected!");
            }
            preprocessedData = runTimed(() -> dataPreprocessor.simplePreprocessAndMerge(data, targetValueIndex), "Preparing data");
        } else {
            preprocessedData = runTimed(() -> dataPreprocessor.simplePreprocess(data), "Preparing data");
        }

        var attributeSelection = runTimed(() -> featureEvaluator.selectAttributes(preprocessedData), "Selecting attributes");
        directDependencies = featureEvaluator.findDirectDependencies(data, attributeSelection);
        highlyCorrelatedAttributes = featureEvaluator.findHighlyCorrelatedAttributes(data, attributeSelection);
        var reducedData = featureEvaluator.retainTop50Attributes(preprocessedData, attributeSelection, suspectedDependencies);
        this.processedData = dataPreprocessor.replaceMissingStringValuesWithConstant(reducedData);

        var dataWithSelectedFeatures = runTimed(() -> featureEvaluator.retainImportantFeatures(processedData, suspectedDependencies), "Calculating feature scores");
        selectedFeatures = IntStream.range(0, dataWithSelectedFeatures.numAttributes())
                .mapToObj(i -> dataWithSelectedFeatures.attribute(i).name())
                .collect(joining("\n"));

        var treeAndRemovedAttributes = runTimed(() -> treeClassifier.buildJ48Tree(dataWithSelectedFeatures), "Building J48 tree");
        tree = treeAndRemovedAttributes.getLeft();
        ignoredAttributes.addAll(treeAndRemovedAttributes.getRight());
        rules = tree.toString();

        stumps = runTimed(() -> treeClassifier.buildStumpsForAttributes(dataWithSelectedFeatures, suspectedDependencies), "Checking suspected dependencies");

        return getTemplateParameters();
    }


    private Map<String, Object> doRunSubsequentEvaluation(List<String> newlyIgnoredAttributes) throws Exception {
        ignoredAttributes.addAll(newlyIgnoredAttributes);
        processedData = dataPreprocessor.remove(newlyIgnoredAttributes, processedData);

        var dataWithSelectedFeatures = runTimed(() -> featureEvaluator.retainImportantFeatures(processedData, suspectedDependencies), "Calculating feature scores");
        selectedFeatures = IntStream.range(0, dataWithSelectedFeatures.numAttributes())
                .mapToObj(i -> dataWithSelectedFeatures.attribute(i).name())
                .collect(joining("\n"));

        var treeAndRemovedAttributes = runTimed(() -> treeClassifier.buildJ48Tree(dataWithSelectedFeatures), "Building J48 tree");
        tree = treeAndRemovedAttributes.getLeft();
        ignoredAttributes.addAll(treeAndRemovedAttributes.getRight());
        processedData = dataPreprocessor.remove(treeAndRemovedAttributes.getRight(), processedData);
        rules = tree.toString();

        return getTemplateParameters();
    }

    Map<String, Object> runCrossValidation() {
        try {
            var modelValidation = runTimed(() -> validator.validate(tree, processedData), "Cross-validating tree");
            var result = getTemplateParameters();
            result.put("evaluation", modelValidation.toClassDetailsString());
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private Map<String, Object> getTemplateParameters() throws Exception {
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
        if (!directDependencies.isEmpty()) {
            result.put("directDependencies", directDependencies);
        }
        if (!highlyCorrelatedAttributes.isEmpty()) {
            result.put("correlatedAttributes", highlyCorrelatedAttributes);
        }

        result.put("selectedAttributes", selectedFeatures);
        result.put("tree", getTreeImageTag(tree));
        result.put("rules", rules);
        return result;
    }

    private String getTreeImageTag(Drawable tree) throws Exception {
        var graph = tree.graph();
        var treeBase64 = Base64.getEncoder().encodeToString(Graphviz.fromString(graph).render(Format.SVG).toString().getBytes(StandardCharsets.UTF_8));
        return "<img src=\"data:image/svg+xml;utf8;base64, " + treeBase64 + "\"/>";
    }

    private List<String> getAttributesSorted() {
        return IntStream.range(0, processedData.numAttributes())
                .mapToObj(i -> processedData.attribute(i).name())
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .collect(Collectors.toList());
    }
}
