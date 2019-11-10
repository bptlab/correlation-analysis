package de.hpi.bpt.evaluation.decisiontree;

import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import org.apache.commons.lang3.tuple.Pair;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.j48.BinC45Split;
import weka.classifiers.trees.j48.ClassifierTree;
import weka.core.Attribute;
import weka.core.Drawable;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;

public class DecisionTreeClassifier {

    public Pair<TraversableJ48, Set<String>> buildJ48Tree(Instances data) {
        try {
            TraversableJ48 classifier = classify(data);

            var removedAttributes = new HashSet<String>();

            Set<Integer> obviousAttributeIndices = collectObviousAttributeIndices(classifier);

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

    private TraversableJ48 classify(Instances data) throws Exception {
        var classifier = new TraversableJ48();
        classifier.setMinNumObj(Math.min(100, data.size() / 100));
        classifier.setBinarySplits(true);
        classifier.buildClassifier(data);
        return classifier;
    }

    private Set<Integer> collectObviousAttributeIndices(TraversableJ48 classifier) {
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

            for (var son : currentNode.getSons()) {
                queue.addLast(son);
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

    private J48 buildStumpForAttribute(Instances data, Attribute attribute) {
        try {
            var classifier = new J48();
            classifier.setMinNumObj(Math.min(100, data.size() / 100));

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
                var stump = buildStumpForAttribute(removed, removed.attribute(attributeName));

                result += getTreeImageTag(stump) + "\n";
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private String getTreeImageTag(Drawable tree) throws Exception {
        var treeBase64 = Base64.getEncoder().encodeToString(Graphviz.fromString(tree.graph()).render(Format.SVG).toString().getBytes(StandardCharsets.UTF_8));
        return "<img src=\"data:image/svg+xml;utf8;base64, " + treeBase64 + "\"/>";
    }
}
