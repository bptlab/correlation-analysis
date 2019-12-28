package de.hpi.bpt.correlationanalysis.framework;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Instances;

import java.util.ArrayList;

public class ClassifierMetrics {

    /**
     * Return accuracy metrics for a given classifier and data set.
     */
    public Evaluation getMetrics(Classifier classifier, Instances data) {
        try {
            var evaluation = new Evaluation(data);
            evaluation.setPriors(data);

            var metricsToDisplay = new ArrayList<String>();
            metricsToDisplay.add("correct");
            metricsToDisplay.add("incorrect");
            metricsToDisplay.add("correlation");
            metricsToDisplay.add("tp rate");
            metricsToDisplay.add("fp rate");
            metricsToDisplay.add("precision");
            metricsToDisplay.add("recall");
            metricsToDisplay.add("f-measure");

            evaluation.setMetricsToDisplay(metricsToDisplay);
            evaluation.evaluateModel(classifier, data);

            return evaluation;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
