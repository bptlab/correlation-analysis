package de.hpi.bpt;

import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import weka.classifiers.CostMatrix;
import weka.classifiers.Evaluation;
import weka.classifiers.meta.AdaBoostM1;
import weka.classifiers.meta.CostSensitiveClassifier;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.REPTree;
import weka.core.Instances;

import java.io.File;
import java.io.IOException;
import java.util.Random;

class DecisionTreeClassifier {

    String buildDecisionRules(Instances data) {
        try {
            var classifier = new J48();
            classifier.setConfidenceFactor(0.5f);
            classifier.buildClassifier(data);


//            var boost = new AdaBoostM1();
//            boost.setClassifier(classifier);
//            boost.buildClassifier(data);

            return classifier.graph();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    void buildDecisionRulesBoosting(Instances data) {
        try {
            var classifier = new J48();
            var boost = new AdaBoostM1();
            boost.setClassifier(classifier);
            var eval = new Evaluation(data);
            eval.crossValidateModel(boost, data, 10, new Random());
            boost.buildClassifier(data);
            writeToFile(((J48) boost.getClassifier()).graph(), "boosted");
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    void buildDecisionRulesCostSensitive(Instances data) {
        try {
            var classifier = new REPTree();
            var costSensitive = new CostSensitiveClassifier();
            costSensitive.setClassifier(classifier);
            var costMatrix = new CostMatrix(2);
            costMatrix.setElement(0, 1, 10d);
            costSensitive.setCostMatrix(costMatrix);
            var eval = new Evaluation(data);
            eval.crossValidateModel(costSensitive, data, 10, new Random());
            costSensitive.buildClassifier(data);
            writeToFile(costSensitive.graph(), "cost_sensitive");
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    void buildStumpForEachAttribute(Instances data) {
        try {
            var oneR = new MyOneR();
            oneR.buildClassifier(data);
            var attributes = data.enumerateAttributes();
            while (attributes.hasMoreElements()) {
                var attribute = attributes.nextElement();

                var attributeRule = oneR.newRule(attribute, data);
                if (attributeRule.percentCorrect() > 80) {
                    System.out.println(attributeRule.valuesCorrelatingTo("false"));
                }
//                var remove = new Remove();
//                remove.setInvertSelection(true);
//                remove.setAttributeIndicesArray(new int[]{data.classIndex(), attribute.index()});
//                remove.setInputFormat(data);
//                var filteredData = Filter.useFilter(data, remove);

//                var classifier = new CostSensitiveClassifier();
////                classifier.setClassifier(new DecisionStump());
//                classifier.setClassifier(new OneR());
//                var costMatrix = new CostMatrix(2);
//                costMatrix.setElement(0, 1, 60d);
//                classifier.setCostMatrix(costMatrix);
//
//                var eval = new Evaluation(filteredData);
//                eval.crossValidateModel(classifier, data, 10, new Random());
//                if (eval.pctCorrect() > 75) {
//                    classifier.buildClassifier(filteredData);
//                    System.out.println(classifier.toString());
//                    System.out.println("---------------------------------------------------");
//                }
//
//                classifier.newRule()
//


//                System.out.println(eval.toClassDetailsString());
//                System.out.println("---------------------------------------------------");
//                System.out.println(eval.toSummaryString());
//                System.out.println("===================================================");
//                writeToFile(classifier.graph(), attribute.name());
            }
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
