package de.hpi.bpt.evaluation.decisiontree;

import weka.classifiers.CostMatrix;
import weka.classifiers.meta.AdaBoostM1;
import weka.classifiers.meta.CostSensitiveClassifier;
import weka.classifiers.rules.PART;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.REPTree;
import weka.core.Instances;

public class DecisionTreeClassifier {

    public J48 buildJ48Tree(Instances data) {
        try {
            var classifier = new J48();
            classifier.buildClassifier(data);

            return classifier;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public PART buildPARTRules(Instances data) {
        try {
            var classifier = new PART();
            classifier.buildClassifier(data);
            return classifier;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public REPTree buildREPTree(Instances data) {
        try {
            var classifier = new REPTree();
            classifier.buildClassifier(data);
            return classifier;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public J48 buildStumpForAttribute(Instances data) {
        try {
            var classifier = new J48();
            classifier.setUnpruned(true);
            classifier.setCollapseTree(false);
            classifier.buildClassifier(data);

            return classifier;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public String buildBoostedJ48Tree(Instances data) {
        try {
            var classifier = new J48();
            var boost = new AdaBoostM1();
            boost.setClassifier(classifier);
            boost.buildClassifier(data);
            return ((J48) boost.getClassifier()).graph();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public String buildCostSensitiveREPTree(Instances data) {
        try {
            var classifier = new REPTree();
            var costSensitive = new CostSensitiveClassifier();
            costSensitive.setClassifier(classifier);
            var costMatrix = new CostMatrix(2);
            costMatrix.setElement(0, 1, 10d);
            costSensitive.setCostMatrix(costMatrix);
//            var eval = new Evaluation(data);
//            eval.crossValidateModel(costSensitive, data, 10, new Random());
            costSensitive.buildClassifier(data);
            return costSensitive.graph();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
