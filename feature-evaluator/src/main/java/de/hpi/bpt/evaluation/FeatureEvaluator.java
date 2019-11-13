package de.hpi.bpt.evaluation;

import weka.attributeSelection.*;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toSet;

public class FeatureEvaluator {

    /**
     * Performs CfsSubsetEvaluation and retains the selected attributes
     */
    public Instances retainImportantFeatures(Instances data, Set<String> suspectedDependencies) {
        try {
            var attributesToKeep = suspectedDependencies.stream().map(attName -> data.attribute(attName).index()).collect(toSet());
            var attributeSelection = new AttributeSelection();

            var evaluator = new CfsSubsetEval();
            evaluator.setPoolSize(4);
            evaluator.setNumThreads(4);
            var search = new BestFirst();
            attributeSelection.setEvaluator(evaluator);
            attributeSelection.setSearch(search);

            attributeSelection.SelectAttributes(data);

            var selectedAttributes = attributeSelection.selectedAttributes();
            var attributesToKeepCombined = IntStream.concat(attributesToKeep.stream().mapToInt(i -> i),
                    Arrays.stream(selectedAttributes))
                    .distinct().toArray();
            var remove = new Remove();
            remove.setAttributeIndicesArray(attributesToKeepCombined);
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
            var evaluator = new SymmetricalUncertAttributeEval();
//            var evaluator = new CorrelationAttributeEval();
//            var evaluator = new GainRatioAttributeEval();
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

    public Instances retainTopAttributes(Instances data, AttributeSelection attributeSelection, Set<String> suspectedDependencies) {
        try {
            var rankedAttributes = attributeSelection.rankedAttributes();

            var attributesToKeep = IntStream.concat(
                    IntStream.concat(
                            suspectedDependencies.stream().mapToInt(attName -> data.attribute(attName).index()),
                            IntStream.range(0, rankedAttributes.length)
                                    .filter(i -> rankedAttributes[i][1] > 0.05 && rankedAttributes[i][1] < 1.0)
                                    .map(i -> (int) rankedAttributes[i][0])),
                    IntStream.of(data.classIndex()))
                    .distinct()
                    .sorted()
                    .toArray();

            var remove = new Remove();
            remove.setAttributeIndicesArray(attributesToKeep);
            remove.setInvertSelection(true);
            remove.setInputFormat(data);
            return Filter.useFilter(data, remove);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }

    }
}
