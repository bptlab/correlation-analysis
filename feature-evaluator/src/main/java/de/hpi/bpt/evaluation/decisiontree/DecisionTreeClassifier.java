package de.hpi.bpt.evaluation.decisiontree;

import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import weka.classifiers.CostMatrix;
import weka.classifiers.Evaluation;
import weka.classifiers.meta.AdaBoostM1;
import weka.classifiers.meta.CostSensitiveClassifier;
import weka.classifiers.rules.PART;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.REPTree;
import weka.core.Instances;

import java.io.File;
import java.io.IOException;
import java.util.Random;

public class DecisionTreeClassifier {

    public String buildJ48Tree(Instances data) {
        try {
            var classifier = new J48();
//            classifier.setConfidenceFactor(0.5f);
            classifier.buildClassifier(data);

            return classifier.graph();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public String buildPARTRules(Instances data) {
        try {
            var classifier = new PART();
            classifier.buildClassifier(data);
            return classifier.toString();
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

    private void writeToFile(String graph, String name) {
        try {
            Graphviz.fromString(graph).render(Format.SVG).toFile(new File("/home/jonas/Data/Macif/incidents/" + name + "_" + "tree.svg"));
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
