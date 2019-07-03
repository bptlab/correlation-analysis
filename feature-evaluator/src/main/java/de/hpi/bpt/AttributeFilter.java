package de.hpi.bpt;

import weka.core.Instances;

import java.util.Set;

class AttributeFilter {
    void filterImportantAttributes(Instances data, Set<String> importantAttributes) {

        boolean inProgress = true;
        while (inProgress) {
            inProgress = false;
            for (int i = 0; i < data.numAttributes(); i++) {
                if (!data.classAttribute().equals(data.attribute(i)) && !importantAttributes.contains(data.attribute(i).name())) {
                    data.deleteAttributeAt(i);
                    inProgress = true;
                    break;
                }
            }
        }
    }
}
