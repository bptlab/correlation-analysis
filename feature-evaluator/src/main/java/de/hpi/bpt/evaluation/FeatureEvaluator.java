package de.hpi.bpt.evaluation;

import weka.attributeSelection.*;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

import java.util.Arrays;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toSet;

public class FeatureEvaluator {

    /**
     * Performs CfsSubsetEvaluation and retains the selected attributes
     */
    public Instances retainImportantFeatures(Instances data) {
        try {
            var attributeSelection = new AttributeSelection();

            var evaluator = new CfsSubsetEval();
            var search = new BestFirst();
            attributeSelection.setEvaluator(evaluator);
            attributeSelection.setSearch(search);

            attributeSelection.SelectAttributes(data);
            return attributeSelection.reduceDimensionality(data);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public Instances retainFeaturesWithCommonValues(Instances data, int[] attributeIndices) {
        try {
            var toRetain = Arrays.stream(attributeIndices).boxed().collect(toSet());

            for (int i = 0; i < data.numAttributes(); i++) {
                var attribute = data.attribute(i);
                if (attribute.numValues() != 2) {
                    toRetain.add(i);
                }
            }

            var remove = new Remove();
            remove.setAttributeIndicesArray(toRetain.stream().mapToInt(i -> i).toArray());
            remove.setInvertSelection(true);
            remove.setInputFormat(data);
            return Filter.useFilter(data, remove);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }


    public AttributeSelection selectAttributes(Instances data) {
        try {
            var attributeSelection = new AttributeSelection();
            var ranker = new Ranker();
//            var evaluator = new SymmetricalUncertAttributeEval();
            var evaluator = new CorrelationAttributeEval();
            attributeSelection.setEvaluator(evaluator);
            attributeSelection.setSearch(ranker);

            attributeSelection.SelectAttributes(data);
            return attributeSelection;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public String findDirectDependencies(Instances data, AttributeSelection attributeSelection) {
        try {
            var rankedAttributes = attributeSelection.rankedAttributes();
            var directDependencyAttributeIndices = IntStream.range(0, rankedAttributes.length)
                    .filter(i -> rankedAttributes[i][1] == 1.0)
                    .map(i -> (int) rankedAttributes[i][0])
                    .toArray();

            return Arrays.stream(directDependencyAttributeIndices).mapToObj(index -> data.attribute(index).name()).collect(joining("\n"));
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public String findHighlyCorrelatedAttributes(Instances data, AttributeSelection attributeSelection) {
        try {
            var rankedAttributes = attributeSelection.rankedAttributes();
            var directDependencyAttributeIndices = IntStream.range(0, rankedAttributes.length)
                    .filter(i -> rankedAttributes[i][1] > 0.9 && rankedAttributes[i][1] != 1.0)
                    .map(i -> (int) rankedAttributes[i][0])
                    .toArray();

            return Arrays.stream(directDependencyAttributeIndices).mapToObj(index -> data.attribute(index).name()).collect(joining("\n"));
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public Instances removeNonInterestingAttributes(Instances data, AttributeSelection attributeSelection) {
        try {
            var rankedAttributes = attributeSelection.rankedAttributes();
            var directDependencyAttributeIndices = IntStream.range(0, rankedAttributes.length)
                    .filter(i -> rankedAttributes[i][1] == 1.0)
                    .map(i -> (int) rankedAttributes[i][0])
                    .toArray();

            var almostNoDependencyAttributeIndices = IntStream.range(0, rankedAttributes.length)
                    .filter(i -> rankedAttributes[i][1] < 0.01)
                    .map(i -> (int) rankedAttributes[i][0])
                    .toArray();

            var remove = new Remove();
            remove.setAttributeIndicesArray(IntStream.concat(Arrays.stream(directDependencyAttributeIndices), Arrays.stream(almostNoDependencyAttributeIndices)).toArray());
            remove.setInputFormat(data);
            return Filter.useFilter(data, remove);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }

    }
}
