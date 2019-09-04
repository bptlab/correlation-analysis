package de.hpi.bpt.evaluation;

import de.hpi.bpt.FeatureEvaluationRunner;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.clusterers.SimpleKMeans;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;
import weka.filters.unsupervised.instance.RemoveWithValues;

import java.util.Random;

public class Clusterer {

    public String clusterPositiveInstances(Classifier tree, Instances data) {
        try {
            var evaluation = new Evaluation(data);
            evaluation.crossValidateModel(tree, data, 10, new Random());
//        if (evaluation.truePositiveRate(data3.classAttribute().indexOfValue(TARGET_VALUE)) < 0.99) { // TODO ratio
            var removeWithValues = new RemoveWithValues();
            removeWithValues.setAttributeIndex("" + (data.classIndex() + 1));
            if (data.classAttribute().isNominal()) {
                removeWithValues.setNominalIndicesArr(new int[]{data.classAttribute().indexOfValue(FeatureEvaluationRunner.TARGET_VALUE)});
            } else {
                throw new RuntimeException("Clustering for numeric target not yet supported");
            }
            removeWithValues.setInvertSelection(true);
            removeWithValues.setInputFormat(data);
            var singleClassData = Filter.useFilter(data, removeWithValues);

            var removeClassAttribute = new Remove();
            removeClassAttribute.setAttributeIndicesArray(new int[]{singleClassData.classIndex()});
            removeClassAttribute.setInputFormat(singleClassData);
            singleClassData = Filter.useFilter(singleClassData, removeClassAttribute);

            var simpleKMeans = new SimpleKMeans();
            simpleKMeans.buildClusterer(singleClassData);
            return simpleKMeans.toString();
//        } else {
//            return emptyList();
//        }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
