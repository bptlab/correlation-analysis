package de.hpi.bpt;

import weka.classifiers.trees.REPTree;
import weka.core.Instances;

class DecisionTreeClassifier {

    String buildDecisionRules(Instances data) {
        try {
            REPTree tree = new REPTree();
            tree.setMaxDepth(3);
            tree.buildClassifier(data);
            return tree.graph();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
