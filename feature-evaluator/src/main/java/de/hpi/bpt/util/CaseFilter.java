package de.hpi.bpt.util;

import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

import java.util.Set;

public class CaseFilter {

    public Instances filterImportantAttributes(Instances data, Set<Integer> importantAttributes) {
        return filterImportantAttributes(data, importantAttributes, false);
    }

    public Instances filterImportantAttributesKeepingClass(Instances data, Set<Integer> importantAttributes) {
        return filterImportantAttributes(data, importantAttributes, true);
    }

    private Instances filterImportantAttributes(Instances data, Set<Integer> importantAttributes, boolean keepClassAttribute) {
        try {
            var remove = new Remove();
            remove.setInvertSelection(true);

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

    public Instances removeWithWrongClassValue(Instances data, String desiredClassValue) {
        var newData = new Instances(data);
        newData.setClass(data.classAttribute());

        var classIndex = newData.classIndex();
        var targetValueIndex = newData.instance(0).attribute(classIndex).indexOfValue(desiredClassValue);

        newData.removeIf(instance -> !(instance.value(classIndex) == targetValueIndex));
        return newData;
    }
}
