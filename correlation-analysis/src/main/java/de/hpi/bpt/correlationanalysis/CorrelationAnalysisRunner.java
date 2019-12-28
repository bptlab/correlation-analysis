package de.hpi.bpt.correlationanalysis;

import de.hpi.bpt.correlationanalysis.framework.ClassifierMetrics;
import de.hpi.bpt.correlationanalysis.framework.DataPreprocessor;
import de.hpi.bpt.correlationanalysis.framework.FeatureSelector;
import de.hpi.bpt.correlationanalysis.framework.decisiontree.DecisionTreeClassifier;
import de.hpi.bpt.correlationanalysis.framework.decisiontree.GraphableJ48;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import weka.core.Drawable;
import weka.core.Instances;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static de.hpi.bpt.correlationanalysis.util.TimeTracker.runTimed;
import static java.util.stream.Collectors.joining;

/**
 * Performs the analysis steps as described in the thesis.
 */
class CorrelationAnalysisRunner {

    private final DataPreprocessor dataPreprocessor = new DataPreprocessor();
    private final FeatureSelector featureEvaluator = new FeatureSelector();
    private final DecisionTreeClassifier treeClassifier = new DecisionTreeClassifier();
    private final ClassifierMetrics validator = new ClassifierMetrics();
    private Instances processedData;
    private String projectName;
    private String targetAttribute;
    private Optional<String> targetValue;
    private Set<GraphableJ48> stumps = new HashSet<>();
    private Set<String> ignoredAttributes = new HashSet<>();
    private Set<String> suspectedCorrelations = new HashSet<>();
    private String directDependencies;
    private String highlyCorrelatedAttributes;
    private String selectedFeatures;
    private GraphableJ48 tree;
    private String rules;

    CorrelationAnalysisRunner projectName(String projectName) {
        this.projectName = projectName;
        return this;
    }

    CorrelationAnalysisRunner targetAttribute(String targetAttribute) {
        this.targetAttribute = targetAttribute;
        return this;
    }


    CorrelationAnalysisRunner targetValue(Optional<String> targetValue) {
        this.targetValue = targetValue;
        return this;
    }

    CorrelationAnalysisRunner suspectedCorrelations(List<String> suspectedCorrelations) {
        this.suspectedCorrelations.addAll(suspectedCorrelations);
        return this;
    }

    void reset() {
        ignoredAttributes.clear();
        suspectedCorrelations.clear();
        stumps.clear();
    }

    Map<String, Object> runFirstAnalysis(Instances data) {
        try {
            return doRunFirstAnalysis(data);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    Map<String, Object> runSubsequentAnalysis(List<String> newlyIgnoredAttributes, String newlyIgnoredAttributesContaining) {
        try {
            return doRunSubsequentAnalysis(newlyIgnoredAttributes, newlyIgnoredAttributesContaining);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private Map<String, Object> doRunFirstAnalysis(Instances data) throws Exception {
        data.setClass(data.attribute(targetAttribute));
        Instances preprocessedData;

        if (targetValue.isPresent()) {
            preprocessedData = runTimed(() -> dataPreprocessor.simplePreprocessAndMerge(data, targetValue.get()), "Preparing data");
        } else {
            preprocessedData = runTimed(() -> dataPreprocessor.simplePreprocess(data), "Preparing data");
        }

        var attributeSelection = runTimed(() -> featureEvaluator.selectAttributes(preprocessedData), "Selecting attributes");
        directDependencies = featureEvaluator.findDirectDependencies(data, attributeSelection);
        highlyCorrelatedAttributes = featureEvaluator.findHighlyCorrelatedAttributes(data, attributeSelection);
        var reducedData = featureEvaluator.retainAttributes(preprocessedData, attributeSelection, suspectedCorrelations);
        this.processedData = dataPreprocessor.replaceMissingStringValuesWithConstant(reducedData);

        return runClassification();
    }

    private Map<String, Object> doRunSubsequentAnalysis(List<String> newlyIgnoredAttributes, String newlyIgnoredAttributesContaining) throws Exception {
        var toRemove = new HashSet<>(newlyIgnoredAttributes);

        if (!newlyIgnoredAttributesContaining.isBlank()) {
            var alsoToIgnore = newlyIgnoredAttributesContaining.split(",");
            var attributeNamesAlsoToIgnore = IntStream.range(0, processedData.numAttributes())
                    .mapToObj(i -> processedData.attribute(i).name())
                    .filter(name -> {
                        for (String s : alsoToIgnore) {
                            if (name.contains(s)) {
                                return true;
                            }
                        }
                        return false;
                    })
                    .collect(Collectors.toList());

            toRemove.addAll(attributeNamesAlsoToIgnore);
        }

        processedData = dataPreprocessor.remove(toRemove, processedData);
        ignoredAttributes.addAll(toRemove);

        return runClassification();
    }

    private Map<String, Object> runClassification() throws Exception {
        // Perform CFS. Might be computationally expensive and not worth it - remove if desired.
        var dataWithSelectedFeatures = runTimed(() -> featureEvaluator.performCfs(processedData, suspectedCorrelations), "Calculating feature scores");
        
        selectedFeatures = IntStream.range(0, dataWithSelectedFeatures.numAttributes())
                .mapToObj(i -> dataWithSelectedFeatures.attribute(i).name())
                .collect(joining("\n"));

        var treeAndRemovedAttributes = runTimed(() -> treeClassifier.buildJ48Tree(dataWithSelectedFeatures), "Building J48 tree");
        tree = treeAndRemovedAttributes.getLeft();
        ignoredAttributes.addAll(treeAndRemovedAttributes.getRight());
        processedData = dataPreprocessor.remove(treeAndRemovedAttributes.getRight(), processedData);

        rules = tree.toString();
        if (stumps.isEmpty()) {
            stumps.addAll(runTimed(() -> treeClassifier.buildStumpsForAttributes(dataWithSelectedFeatures, suspectedCorrelations), "Checking suspected dependencies"));
        }

        var modelValidation = runTimed(() -> validator.getMetrics(tree, dataWithSelectedFeatures), "Cross-validating tree");

        var result = getTemplateParameters();
        result.put("evaluation", modelValidation.toSummaryString() + "\n" + modelValidation.toClassDetailsString());
        return result;
    }


    private Map<String, Object> getTemplateParameters() {
        var result = new HashMap<String, Object>();
        result.put("projectName", projectName);
        result.put("targetAttribute", targetAttribute);
        result.put("attributes", getAttributesSorted());
        if (!ignoredAttributes.isEmpty()) {
            result.put("ignoredAttributes", ignoredAttributes);
        }
        if (!suspectedCorrelations.isEmpty()) {
            result.put("suspectedDependencies", suspectedCorrelations);
            result.put("assumptionStumps", stumps.stream().map(this::getTreeImageTag).collect(joining("\n\n")));
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

    private String getTreeImageTag(Drawable tree) {
        try {
            var graph = tree.graph();
            var treeBase64 = Base64.getEncoder().encodeToString(Graphviz.fromString(graph).render(Format.SVG).toString().getBytes(StandardCharsets.UTF_8));
            return "<img src=\"data:image/svg+xml;utf8;base64, " + treeBase64 + "\"/>";
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private List<String> getAttributesSorted() {
        return IntStream.range(0, processedData.numAttributes())
                .mapToObj(i -> processedData.attribute(i).name())
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .collect(Collectors.toList());
    }
}
