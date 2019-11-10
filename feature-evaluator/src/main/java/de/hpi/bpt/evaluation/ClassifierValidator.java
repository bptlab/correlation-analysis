package de.hpi.bpt.evaluation;

import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Instances;

import java.util.ArrayList;

public class ClassifierValidator {

    public Evaluation validate(Classifier classifier, Instances data) {
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
            evaluation.evaluateModel(AbstractClassifier.makeCopy(classifier), data);

            return evaluation;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
