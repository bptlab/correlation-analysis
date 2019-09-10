package de.hpi.bpt.util;

import de.hpi.bpt.FeatureEvaluationRunner;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.instance.RemoveWithValues;

public class DataSplitter {

    public Instances removeInstancesWithWrongClass(Instances data) {
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
            return Filter.useFilter(data, removeWithValues);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
