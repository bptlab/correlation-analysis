package de.hpi.bpt.evaluation;

import weka.clusterers.SimpleKMeans;
import weka.core.Attribute;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.DateToNumeric;
import weka.filters.unsupervised.attribute.Remove;

import java.util.Enumeration;

import static java.util.stream.Collectors.toSet;

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
            simpleKMeans.setNumClusters(3);
            simpleKMeans.buildClusterer(singleClassData);

            return filterCommonValuesBetweenCentroids(singleClassData.enumerateAttributes(), simpleKMeans.getClusterCentroids());
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private String filterCommonValuesBetweenCentroids(Enumeration<Attribute> attributes, Instances centroids) {
        var result = new StringBuilder();

        while (attributes.hasMoreElements()) {
            var attribute = attributes.nextElement();
            var distinctValues = centroids.stream()
                    .map(centroid -> centroid.value(attribute))
                    .collect(toSet());
            if (distinctValues.size() == 1) {
                var distinctValue = distinctValues.iterator().next();
                if (attribute.isNominal()) {
                    if (!attribute.value(distinctValue.intValue()).equals("f")) {
                        result.append(attribute.name()).append(": ").append(attribute.value(distinctValue.intValue())).append("\n");
                    }
                } else {
                    result.append(attribute.name()).append(": ").append(distinctValue).append("\n");
                }
            }
        }

        return result.toString();
    }
}
