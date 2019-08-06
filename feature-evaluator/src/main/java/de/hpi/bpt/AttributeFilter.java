package de.hpi.bpt;

import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

import java.util.Set;

class AttributeFilter {

    Instances filterImportantAttributes(Instances data, Set<Integer> importantAttributes) {
        return filterImportantAttributes(data, importantAttributes, false);
    }

    Instances filterImportantAttributesKeepingClass(Instances data, Set<Integer> importantAttributes) {
        return filterImportantAttributes(data, importantAttributes, true);
    }

    private Instances filterImportantAttributes(Instances data, Set<Integer> importantAttributes, boolean keepClassAttribute) {
        try {
            var remove = new Remove();
            remove.setInvertSelection(true);

//            var importantAttributeIndices = importantAttributes.stream().map(name -> data.attribute(name).index())
//                    .sorted()
//                    .map(String::valueOf)
//                    .collect(Collectors.joining(","));
            if (keepClassAttribute) {
                importantAttributes.add(data.classIndex());
            }

            remove.setAttributeIndicesArray(importantAttributes.stream().mapToInt(i -> i).sorted().toArray());
            remove.setInputFormat(data);
            return Filter.useFilter(data, remove);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
