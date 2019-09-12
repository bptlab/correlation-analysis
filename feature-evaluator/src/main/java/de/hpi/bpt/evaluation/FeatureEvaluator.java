package de.hpi.bpt.evaluation;

import org.apache.commons.lang3.tuple.Pair;
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

            try {
                var evaluator = new CfsSubsetEval();
                var search = new BestFirst();
                attributeSelection.setEvaluator(evaluator);
                attributeSelection.setSearch(search);

                attributeSelection.SelectAttributes(data);
            } catch (OutOfMemoryError e) {
                var evaluator = new SymmetricalUncertAttributeEval();
                var search = new Ranker();
                attributeSelection.setEvaluator(evaluator);
                attributeSelection.setSearch(search);
                attributeSelection.SelectAttributes(data);
            }
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

    /**
     * Removes attributes that correlate directly with the class attribute.
     * Prints their names as String, separated by newlines.
     */
    public Pair<String, Instances> findAndRemoveDirectDependencies(Instances data) {
        try {
            var attributeSelection = new AttributeSelection();
            var ranker = new Ranker();
            var evaluator = new SymmetricalUncertAttributeEval();
            attributeSelection.setEvaluator(evaluator);
            attributeSelection.setSearch(ranker);

            attributeSelection.SelectAttributes(data);
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
            return Pair.of(
                    Arrays.stream(directDependencyAttributeIndices).mapToObj(index -> data.attribute(index).name()).collect(joining("\n")),
                    Filter.useFilter(data, remove)
            );

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
