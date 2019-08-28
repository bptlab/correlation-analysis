package de.hpi.bpt.evaluation.regression;

import weka.classifiers.functions.Logistic;
import weka.core.Instances;

public class RegressionAnalysis {

    public String buildLogisticClassifier(Instances data) {
        try {
            var logistic = new Logistic();
            logistic.buildClassifier(data);
            return logistic.toString();
//            return logistic.toPMML(data);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
