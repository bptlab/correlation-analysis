package de.hpi.bpt.evaluation;

import de.hpi.bpt.FeatureEvaluationRunner;
import weka.clusterers.SimpleKMeans;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;
import weka.filters.unsupervised.instance.RemoveWithValues;

public class Clusterer {

    public String clusterPositiveInstances(Instances data) {
        try {
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
            simpleKMeans.setNumClusters(5);
            simpleKMeans.buildClusterer(singleClassData);
            return simpleKMeans.toString();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
