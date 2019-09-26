package de.hpi.bpt;

import ro.pippo.core.Application;
import weka.core.Instances;
import weka.core.converters.ArffLoader;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class FeatureEvaluationApplication extends Application {

    private Instances data;
    private String projectName;

    @Override
    protected void onInit() {

        GET("/", routeContext -> routeContext.render("upload-page"));

        POST("/upload", routeContext -> {
            var fileItem = routeContext.getRequest().getFile("caseLog");
            this.projectName = routeContext.getParameter("projectName").toString();

            try (var inputStream = fileItem.getInputStream()) {
                var arffLoader = new ArffLoader();
                arffLoader.setSource(inputStream);
                this.data = arffLoader.getDataSet();
            } catch (IOException e) {
                throw new RuntimeException(e.getMessage(), e);
            }

            routeContext.render("select-page",
                    new HashMap<>(
                            Map.of(
                                    "projectName", this.projectName,
                                    "TARGET_ATTRIBUTE", "",
                                    "TARGET_VALUE", "",
                                    "IGNORED_ATTRIBUTES", "",
                                    "SUSPECTED_DEPENDENCIES", ""
                            )));
        });

        POST("/select", routeContext -> {
            var targetAttribute = routeContext.getParameter("targetAttribute").toString();
            var targetValue = routeContext.getParameter("targetValue").toString();
            var ignoredAttributes = routeContext.getParameter("ignoredAttributes").toString();
            var suspectedDependencies = routeContext.getParameter("suspectedDependencies").toString();
            var preprocessing = routeContext.getParameter("preprocessing").toString();

            var runner = new FeatureEvaluationRunner();
            var results = runner.runEvaluation(data, targetAttribute, targetValue,
                    Arrays.asList(ignoredAttributes.split(",")),
                    Arrays.asList(suspectedDependencies.split(",")),
                    preprocessing
            );

            results.put("projectName", this.projectName);
            results.put("TARGET_ATTRIBUTE", targetAttribute);
            results.put("TARGET_VALUE", targetValue);
            results.put("IGNORED_ATTRIBUTES", ignoredAttributes);
            results.put("SUSPECTED_DEPENDENCIES", suspectedDependencies);
            routeContext.render("result-page", results);
        });
    }
}
