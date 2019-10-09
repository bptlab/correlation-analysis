package de.hpi.bpt.evaluation.decisiontree;

import weka.classifiers.rules.PART;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

import java.util.Set;

public class DecisionRulesClassifier {

    public PART buildPARTRules(Instances data) {
        try {
            var classifier = new PART();
            classifier.setBinarySplits(true);
            classifier.buildClassifier(data);
            return classifier;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public String buildStumpsForAttributes(Instances data, Set<String> suspectedDependencies) {
        try {
            var result = "";
            for (String attributeName : suspectedDependencies) {
                if (data.attribute(attributeName) == null) {
                    continue;
                }
                var remove = new Remove();
                remove.setAttributeIndicesArray(new int[]{data.classIndex(), data.attribute(attributeName).index()});
                remove.setInvertSelection(true);
                remove.setInputFormat(data);
                var removed = Filter.useFilter(data, remove);
                var rules = buildRulesForAttribute(removed);

                result += rules.toString() + "\n\n";
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private PART buildRulesForAttribute(Instances data) {
        try {
            var classifier = new PART();
            classifier.setBinarySplits(true);
            classifier.setMinNumObj(Math.min(100, data.size() / 10));
//            classifier.setUnpruned(true);
            classifier.buildClassifier(data);

            return classifier;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
