package de.hpi.bpt;

import de.hpi.bpt.evaluation.FeatureEvaluator;
import de.hpi.bpt.evaluation.decisiontree.DecisionTreeClassifier;
import de.hpi.bpt.util.DataPreprocessor;
import ro.pippo.core.Application;
import ro.pippo.core.FileItem;
import weka.core.Instances;
import weka.core.converters.ArffLoader;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static de.hpi.bpt.util.TimeTracker.runTimed;
import static java.util.Collections.emptySet;

public class FeatureEvaluationApplication extends Application {

    private Instances processedData;
    private String projectName;
    private String targetAttribute;
    private String stumps;
    private Set<String> ignoredAttributes = new HashSet<>();
    private Set<String> suspectedDependencies = new HashSet<>();

    @Override
    protected void onInit() {

        GET("/", routeContext -> {
            ignoredAttributes.clear();
            suspectedDependencies.clear();
            routeContext.render("upload-page");
        });

        POST("/upload", routeContext ->

        {
            var fileItem = routeContext.getRequest().getFile("caseLog");
            projectName = routeContext.getParameter("projectName").toString();
            targetAttribute = routeContext.getParameter("targetAttribute").toString();
            var suspectedDependenciesParam = routeContext.getParameter("suspectedDependencies").toString();
            suspectedDependencies.addAll(Arrays.asList(suspectedDependenciesParam.split(",")));

            var results = upload(fileItem);
            routeContext.render("result-page", results);
        });

        POST("/ignore", routeContext ->

        {
            var newIgnoredAttributesParam = routeContext.getParameter("ignoredAttributes").getValues();
            Set<String> newIgnoredAttributes = newIgnoredAttributesParam.length == 0 ? emptySet() : Set.of(newIgnoredAttributesParam);

            ignoredAttributes.addAll(newIgnoredAttributes);

            var results = refine(newIgnoredAttributes);
            if (!suspectedDependencies.isEmpty()) {
                results.put("ASSUMPTION_TREES", stumps);
            }
            routeContext.render("result-page", results);
        });
    }

    private Map<String, Object> upload(FileItem fileItem) {
        var data = runTimed(() -> loadDataFromFile(fileItem), "Loading data from file");
        processedData = runTimed(() -> preProcess(data), "Preprocessing data");
        var results = runEvaluation();

        if (!suspectedDependencies.isEmpty()) {
            stumps = runTimed(this::buildStumps, "Building stumps");
            results.put("ASSUMPTION_TREES", stumps);
        }
        return results;
    }

    private Instances loadDataFromFile(FileItem fileItem) {
        try (var inputStream = fileItem.getInputStream()) {
            var arffLoader = new ArffLoader();
            arffLoader.setSource(inputStream);
            return arffLoader.getDataSet();
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private Instances preProcess(Instances data) {
        var dataPreprocessor = new DataPreprocessor();
        var preprocessedData = runTimed(() -> dataPreprocessor.simplePreprocess(data, targetAttribute), "Preparing data");

        var featureEvaluator = new FeatureEvaluator();
        var attributeSelection = featureEvaluator.selectAttributes(preprocessedData);
        var processedData = featureEvaluator.retainTop50Attributes(preprocessedData, attributeSelection, suspectedDependencies);
        return dataPreprocessor.replaceMissingStringValuesWithConstant(processedData);
    }

    private Map<String, Object> runEvaluation() {
        var runner = new FeatureEvaluationRunner();
        var results = runner.runEvaluation(processedData, suspectedDependencies);

        results.put("projectName", this.projectName);
        results.put("attributes", getAttributesSorted(processedData));
        results.put("TARGET_ATTRIBUTE", targetAttribute);
        results.put("IGNORED_ATTRIBUTES", String.join("\n", ignoredAttributes));
        results.put("SUSPECTED_DEPENDENCIES", String.join("\n", suspectedDependencies));

        return results;
    }

    private String buildStumps() {
        var classifier = new DecisionTreeClassifier();
        return classifier.buildStumpsForAttributes(processedData, suspectedDependencies);
    }

    private Map<String, Object> refine(Set<String> newIgnoredAttributes) {
        var dataPreprocessor = new DataPreprocessor().ignoring(newIgnoredAttributes);
        processedData = runTimed(() -> dataPreprocessor.removeSelectedAttributes(processedData), "Preparing data");

        return runEvaluation();
    }

    private List<String> getAttributesSorted(Instances data) {
        return IntStream.range(0, data.numAttributes())
                .mapToObj(i -> data.attribute(i).name())
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .collect(Collectors.toList());
    }
}
