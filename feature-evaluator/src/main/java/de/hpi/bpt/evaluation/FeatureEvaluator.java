package de.hpi.bpt.evaluation;

import de.hpi.bpt.FeatureEvaluationRunner;
import weka.attributeSelection.*;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

import java.util.Arrays;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.joining;

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

    /**
     * Removes attributes that correlate directly with the class attribute.
     * Prints their names as String, separated by newlines.
     */
    public Instances findAndRemoveDirectDependencies(Instances data) {
        try {
            var attributeSelection = new AttributeSelection();
            var ranker = new Ranker();
            var evaluator = new SymmetricalUncertAttributeEval();
            attributeSelection.setEvaluator(evaluator);
            attributeSelection.setSearch(ranker);

            attributeSelection.SelectAttributes(data);
            var rankedAttributes = attributeSelection.rankedAttributes();
            var attributeIndices = IntStream.range(0, rankedAttributes.length)
                    .filter(i -> rankedAttributes[i][1] == 1.0)
                    .map(i -> (int) rankedAttributes[i][0])
                    .toArray();

            FeatureEvaluationRunner.writeToFile(
                    Arrays.stream(attributeIndices).mapToObj(index -> data.attribute(index).name()).collect(joining("\n")),
                    FeatureEvaluationRunner.FOLDER_PATH + FeatureEvaluationRunner.DIRECT_DEPENDENCIES_OUTPUT_FILE
            );

            var remove = new Remove();
            remove.setAttributeIndicesArray(attributeIndices);
            remove.setInputFormat(data);
            return Filter.useFilter(data, remove);

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
