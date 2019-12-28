package de.hpi.bpt.correlationanalysis.framework.decisiontree;

import org.apache.commons.lang3.tuple.Pair;
import weka.classifiers.trees.j48.BinC45Split;
import weka.classifiers.trees.j48.ClassifierTree;
import weka.core.Attribute;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

import java.util.*;

public class DecisionTreeClassifier {

    /**
     * Learns a decision tree for the given instances, using the WEKA {@link weka.classifiers.trees.J48} classifier.
     * In the process, removes attributes that produce pure leaves (i.e., nodes that do not contain incorrectly
     * classified instances).
     * This is done because these attributes often pose obvious correlations.
     *
     * @return A pair of tree and names of removed attributes.
     */
    public Pair<GraphableJ48, Set<String>> buildJ48Tree(Instances data) {
        try {
            GraphableJ48 classifier = classify(data);

            var removedAttributes = new HashSet<String>();

            var obviousAttributeIndices = collectObviousAttributeIndices(classifier);

            while (!obviousAttributeIndices.isEmpty()) {
                System.out.println("Removing:");
                for (Integer obviousAttributeIndex : obviousAttributeIndices) {
                    var attributeName = data.attribute(obviousAttributeIndex).name();
                    System.out.println("- " + attributeName);
                    removedAttributes.add(attributeName);
                }
                var remove = new Remove();
                remove.setAttributeIndicesArray(obviousAttributeIndices.stream().mapToInt(i -> i).toArray());
                remove.setInputFormat(data);
                data = Filter.useFilter(data, remove);
                classifier = classify(data);
                obviousAttributeIndices = collectObviousAttributeIndices(classifier);
            }

            return Pair.of(classifier, removedAttributes);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private GraphableJ48 classify(Instances data) throws Exception {
        var classifier = new GraphableJ48();

        // Minimum number ob objects per leaf. Currently set to 1% of instances.
        // If desired, decrease for large data sets.
        classifier.setMinNumObj(data.size() / 100);

        // Binary splits for improved readability with nominal attributes.
        classifier.setBinarySplits(true);

        // Set to false for improved performance. According to e.g., https://stackoverflow.com/a/11739494,
        // this has little impact on the results but can have a larger impact on performance.
        classifier.setSubtreeRaising(false);
        classifier.buildClassifier(data);
        return classifier;
    }

    private Set<Integer> collectObviousAttributeIndices(GraphableJ48 classifier) {
        var queue = new ArrayDeque<ClassifierTree>();
        queue.addLast(classifier.getRoot());
        var attIndicesToRemove = new HashSet<Integer>();

        while (!queue.isEmpty()) {
            var currentNode = queue.removeFirst();
            // Leaf
            if (currentNode.isLeaf()) {
                continue;
            }

            // Only one child
            var split = (BinC45Split) currentNode.getLocalModel();
            if (currentNode.getSons().length < 2) {
                attIndicesToRemove.add(split.attIndex());
                continue;
            }

            for (var child : currentNode.getSons()) {
                queue.addLast(child);
            }

            // One child is leaf and has only instances of one class
            var leftChildIncorrect = currentNode.getSons()[0].getLocalModel().distribution().numIncorrect();
            var rightChildIncorrect = currentNode.getSons()[1].getLocalModel().distribution().numIncorrect();
            if (leftChildIncorrect == 0 || rightChildIncorrect == 0) {
                attIndicesToRemove.add(split.attIndex());
            }
        }

        return attIndicesToRemove;
    }

    /**
     * For suspected correlations.
     * For each of the given attributes, builds a decision tree using only said attribute.
     * The resulting tree can show whether the suspected correlation is present.
     *
     * @param data Cases
     * @param suspectedCorrelations Set of names of attributes with suspected correlations
     * @return A list of decision
     */
    public Set<GraphableJ48> buildStumpsForAttributes(Instances data, Set<String> suspectedCorrelations) {
        try {
            var result = new HashSet<GraphableJ48>();
            for (String attributeName : suspectedCorrelations) {
                if (data.attribute(attributeName) == null) {
                    continue;
                }
                var remove = new Remove();
                remove.setAttributeIndicesArray(new int[]{data.classIndex(), data.attribute(attributeName).index()});
                remove.setInvertSelection(true);
                remove.setInputFormat(data);
                var removed = Filter.useFilter(data, remove);
                var stump = buildStumpForAttribute(removed, removed.attribute(attributeName));
                result.add(stump);
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private GraphableJ48 buildStumpForAttribute(Instances data, Attribute attribute) {
        try {
            var classifier = new GraphableJ48();
            classifier.setMinNumObj(data.size() / 100);

            if (attribute.numValues() > 5) {
                // many different values: we hope to find relevant ones via binary splits and drop irrelevant ones via pruning
                classifier.setBinarySplits(true);
            } else {
                // small number of different values: print value distribution for each value
                classifier.setUnpruned(true);
                classifier.setCollapseTree(false);
            }

            classifier.buildClassifier(data);

            return classifier;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
