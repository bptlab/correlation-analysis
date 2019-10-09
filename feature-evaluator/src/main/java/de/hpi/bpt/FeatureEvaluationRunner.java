package de.hpi.bpt;

import de.hpi.bpt.evaluation.CrossValidator;
import de.hpi.bpt.evaluation.FeatureEvaluator;
import de.hpi.bpt.evaluation.decisiontree.DecisionRulesClassifier;
import de.hpi.bpt.evaluation.decisiontree.DecisionTreeClassifier;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import weka.classifiers.Classifier;
import weka.core.Drawable;
import weka.core.Instances;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

import static de.hpi.bpt.util.TimeTracker.runTimed;
import static java.util.stream.Collectors.joining;

class FeatureEvaluationRunner {

    Map<String, Object> runEvaluation(Instances data, Set<String> suspectedDependencies) {
        try {
            return doRunEvaluation(data, suspectedDependencies);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }


    private Map<String, Object> doRunEvaluation(Instances data, Set<String> suspectedDependencies) throws Exception {
        var featureEvaluator = new FeatureEvaluator();
        var treeClassifier = new DecisionTreeClassifier();
        var rulesClassifier = new DecisionRulesClassifier();
        var validator = new CrossValidator();

        var simpleAttributeSelection = runTimed(() -> featureEvaluator.selectAttributes(data), "Selecting attributes");
        var directDependencies = featureEvaluator.findDirectDependencies(data, simpleAttributeSelection);
        var highlyCorrelatedAttributes = featureEvaluator.findHighlyCorrelatedAttributes(data, simpleAttributeSelection);
        var reducedData = featureEvaluator.removeNonInterestingAttributes(data, simpleAttributeSelection, suspectedDependencies);

        var dataWithSelectedFeatures = runTimed(() -> featureEvaluator.retainImportantFeatures(reducedData, suspectedDependencies), "Calculating feature scores");
        var selectedFeatures = IntStream.range(0, dataWithSelectedFeatures.numAttributes())
                .mapToObj(i -> dataWithSelectedFeatures.attribute(i).name())
                .collect(joining("\n"));

        Classifier tree = runTimed(() -> treeClassifier.buildJ48Tree(dataWithSelectedFeatures), "Building J48 tree");
        var treeImage = getTreeImageTag((Drawable) tree);

        var rules = runTimed(() -> rulesClassifier.buildPARTRules(dataWithSelectedFeatures), "Building decision rules");

//        var crossValidation = runTimed(() -> validator.validate(rules, dataWithSelectedFeatures), "Cross-validating tree");

//        var stumps = runTimed(() -> classifier.buildStumpsForAttributes(dataWithSelectedFeatures, suspectedDependencies), "Checking suspected dependencies");

        var result = new HashMap<String, Object>();
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
}
