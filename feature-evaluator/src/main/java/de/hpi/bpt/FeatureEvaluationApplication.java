package de.hpi.bpt;

import ro.pippo.core.Application;
import ro.pippo.core.FileItem;
import weka.core.Instances;
import weka.core.converters.ArffLoader;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static de.hpi.bpt.util.TimeTracker.runTimed;
import static java.util.Collections.emptyList;

public class FeatureEvaluationApplication extends Application {

    private final FeatureEvaluationRunner runner = new FeatureEvaluationRunner();

    @Override
    protected void onInit() {

        GET("/", routeContext -> {
            runner.reset();
            routeContext.render("upload-page");
        });

        POST("/upload", routeContext ->
        {
            var fileItem = routeContext.getRequest().getFile("caseLog");
            var projectName = routeContext.getParameter("projectName").toString();
            var targetAttribute = routeContext.getParameter("targetAttribute").toString();
            var suspectedDependenciesParam = routeContext.getParameter("suspectedDependencies").toString();
            List<String> suspectedDependencies = suspectedDependenciesParam.isEmpty() ? emptyList() : Arrays.asList(suspectedDependenciesParam.split(","));

            var data = runTimed(() -> loadDataFromFile(fileItem), "Loading data from file");
            var results = runner
                    .projectName(projectName)
                    .targetAttribute(targetAttribute)
                    .suspectedDependencies(suspectedDependencies)
                    .runFirstEvaluation(data);

            routeContext.render("result-page", results);
        });

        POST("/ignore", routeContext ->
        {
            var newIgnoredAttributesParam = routeContext.getParameter("ignoredAttributes").getValues();
            var newIgnoredAttributes = Arrays.asList(newIgnoredAttributesParam);
            var results = runner.runSubsequentEvaluation(newIgnoredAttributes);
            routeContext.render("result-page", results);
        });

        GET("/crossvalidate", routeContext -> {
            routeContext.render("result-page", runner.runCrossValidation());
        });
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
}
