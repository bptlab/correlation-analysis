package de.hpi.bpt;

import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

import java.util.Set;

class AttributeFilter {
    Instances filterImportantAttributes(Instances data, Set<String> importantAttributes) {

        try {
            var remove = new Remove();
            remove.setInvertSelection(true);
            var importantAttributeIndices = importantAttributes.stream().mapToInt(name -> data.attribute(name).index()).toArray();
            remove.setAttributeIndicesArray(importantAttributeIndices);
            remove.setInputFormat(data);
            return Filter.useFilter(data, remove);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
