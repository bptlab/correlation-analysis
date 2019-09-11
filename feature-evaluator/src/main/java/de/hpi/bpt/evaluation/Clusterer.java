package de.hpi.bpt.evaluation;

import weka.clusterers.SimpleKMeans;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.DateToNumeric;
import weka.filters.unsupervised.attribute.Remove;

public class Clusterer {

    public String clusterPositiveInstances(Instances singleClassData) {
        try {
            var removeClassAttribute = new Remove();
            removeClassAttribute.setAttributeIndicesArray(new int[]{singleClassData.classIndex()});
            removeClassAttribute.setInputFormat(singleClassData);
            singleClassData = Filter.useFilter(singleClassData, removeClassAttribute);

            var dateToNumeric = new DateToNumeric();
            dateToNumeric.setAttributeIndices("first-last");
            dateToNumeric.setInputFormat(singleClassData);
            singleClassData = Filter.useFilter(singleClassData, dateToNumeric);

            var simpleKMeans = new SimpleKMeans();
            simpleKMeans.setNumClusters(5);
            simpleKMeans.buildClusterer(singleClassData);
            return simpleKMeans.toString();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
