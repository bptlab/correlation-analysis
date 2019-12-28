package de.hpi.bpt.correlationanalysis;

import ro.pippo.core.Application;
import ro.pippo.core.FileItem;
import weka.core.Instances;
import weka.core.converters.ArffLoader;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static de.hpi.bpt.correlationanalysis.util.TimeTracker.runTimed;
import static java.util.Collections.emptyList;

/**
 * Provides the server endpoints.
 * Delivers pages as defined in {@code resources/templates}.
 */
public class CorrelationAnalysisApplication extends Application {

    private final CorrelationAnalysisRunner runner = new CorrelationAnalysisRunner();

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
            Optional<String> targetValue = routeContext.getParameter("targetValue").isEmpty() ? Optional.empty() : Optional.of(routeContext.getParameter("targetValue").toString());
            var suspectedDependenciesParam = routeContext.getParameter("suspectedDependencies").toString();
            List<String> suspectedDependencies = suspectedDependenciesParam.isEmpty() ? emptyList() : Arrays.asList(suspectedDependenciesParam.split(","));

            var data = runTimed(() -> loadDataFromFile(fileItem), "Loading data from file");
            var results = runner
                    .projectName(projectName)
                    .targetAttribute(targetAttribute)
                    .targetValue(targetValue)
                    .suspectedCorrelations(suspectedDependencies)
                    .runFirstAnalysis(data);

            routeContext.render("result-page", results);
        });

        POST("/ignore", routeContext ->
        {
            var newIgnoredAttributes = Arrays.asList(routeContext.getParameter("ignoredAttributes").getValues());
            var newIgnoredAttributesContaining = routeContext.getParameter("ignoredAttributesContaining").toString();
            var results = runner.runSubsequentAnalysis(newIgnoredAttributes, newIgnoredAttributesContaining);
            routeContext.render("result-page", results);
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
