package de.hpi.bpt;

import weka.classifiers.meta.AdaBoostM1;
import weka.classifiers.trees.J48;
import weka.core.Instances;

class DecisionTreeClassifier {

    String buildDecisionRules(Instances data) {
        try {
            var j48 = new J48();
            var boost = new AdaBoostM1();
            boost.setClassifier(j48);
            boost.buildClassifier(data);
            return j48.graph();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
