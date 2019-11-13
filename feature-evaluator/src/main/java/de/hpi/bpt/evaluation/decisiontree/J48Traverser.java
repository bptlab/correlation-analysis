package de.hpi.bpt.evaluation.decisiontree;

import org.apache.commons.lang3.tuple.Pair;
import weka.classifiers.trees.j48.*;
import weka.core.Attribute;
import weka.core.Instances;
import weka.core.Utils;

import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.Date;

class J48Traverser {

    String graph(ClassifierTree root) throws Exception {
        var stack = new ArrayDeque<Pair<ClassifierTree, Integer>>();

        StringBuffer text = new StringBuffer();
        text.append("digraph J48Tree {\n").append("node [shape=record, margin=\"0.25,0.1\"];\n");

        var nodeId = 0;
        var totalNumInstances = numInstancesIn(root);

        graphRoot(root, text);
        if (!root.isLeaf()) {
            var localModel = root.getLocalModel();
            var trainingData = root.getTrainingData();
            var children = root.getSons();
            for (int childIndex = 0; childIndex < children.length; childIndex++) {
                nodeId++;
                createEdgeLabel(text, localModel, 0, nodeId, childIndex, trainingData);
                if (children[childIndex].isLeaf()) {
                    createLeafLabel(text, nodeId, totalNumInstances, children[childIndex], trainingData, childIndex, localModel);
                } else {
                    createNodeLabel(text, nodeId, trainingData, children[childIndex], totalNumInstances);
                    stack.addFirst(Pair.of(children[childIndex], nodeId));
                }
            }
        }

        while (!stack.isEmpty()) {
            var nodeAndId = stack.removeFirst();
            var node = nodeAndId.getLeft();
            var id = nodeAndId.getRight();
            if (!node.isLeaf()) {
                var localModel = node.getLocalModel();
                var trainingData = node.getTrainingData();
                var children = node.getSons();
                for (int childIndex = 0; childIndex < children.length; childIndex++) {
                    nodeId++;
                    createEdgeLabel(text, localModel, id, nodeId, childIndex, trainingData);
                    if (children[childIndex].isLeaf()) {
                        createLeafLabel(text, nodeId, totalNumInstances, children[childIndex], trainingData, childIndex, localModel);
                    } else {
                        createNodeLabel(text, nodeId, trainingData, children[childIndex], totalNumInstances);
                        stack.addFirst(Pair.of(children[childIndex], nodeId));
                    }
                }
            }
        }

        text.append("}\n");
        return text.toString();
    }

    private void graphRoot(ClassifierTree root, StringBuffer text) throws Exception {
        var trainingData = root.getTrainingData();
        if (root.isLeaf()) {
            createLeafLabel(text, 0, numInstancesIn(root), root, trainingData, 0, root.getLocalModel());
        } else {
            createNodeLabel(text, 0, trainingData, root, numInstancesIn(root));
        }
    }

    private void createLeafLabel(StringBuffer text, int nodeId, double totalNumInstances, ClassifierTree leaf, Instances trainingData, int childIndex, ClassifierSplitModel parentLocalModel) throws Exception {
        var numInstances = numInstancesIn(leaf);
        text.append("N").append(nodeId).append(" [shape=record, style=filled, label=\"{").append(Utils.backQuoteChars(parentLocalModel.dumpLabel(childIndex, trainingData)));
        text.append("|").append(String.format("%.2f%%", (double) numInstances / totalNumInstances * 100));
        text.append("}\" ").append("];\n");
    }

    private void createNodeLabel(StringBuffer text, int id, Instances trainingData, ClassifierTree child, int totalNumInstances) {
        var numInstances = numInstancesIn(child);
        text.append("N").append(id).append(" [label=\"{").append(Utils.backQuoteChars(child.getLocalModel().leftSide(trainingData)))
                .append("|").append(String.format("%.2f%%", (double) numInstances / (double) totalNumInstances * 100))
                .append("|").append(Utils.backQuoteChars(createSplitNodeLabel(child, trainingData.classAttribute())))
                .append("}\" ");
        text.append("];\n");
    }

    private void createEdgeLabel(StringBuffer text, ClassifierSplitModel localModel, int sourceId, int targetId, int childIndex, Instances trainingData) {
        var attributeValue = localModel.rightSide(childIndex, trainingData).trim();
        Attribute attribute;
        if (localModel instanceof BinC45Split) {
            attribute = trainingData.attribute(((BinC45Split) localModel).attIndex());
        } else {
            attribute = trainingData.attribute(((C45Split) localModel).attIndex());
        }

        if (attribute.isDate()) {
            var split = attributeValue.split(" ");
            var operand = split[0];
            var timestamp = split[1];
            String dateFormat = "yyyy-MM-dd";
            SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
            attributeValue = operand + " " + sdf.format(new Date(Long.parseLong(timestamp)));
        }
        text.append("N").append(sourceId).append("->").append("N").append(targetId)
                .append(" [label=\"")
                .append(Utils.backQuoteChars(attributeValue))
                .append("\"];\n");
    }

    private String createSplitNodeLabel(ClassifierTree node, Attribute classAttribute) {

        StringBuffer text;

        text = new StringBuffer();
        var numClasses = node.getLocalModel().distribution().numClasses();

        text.append("{");
        for (int i = 0; i < numClasses; i++) {
            Distribution distribution = node.getLocalModel().distribution();
            text.append(classAttribute.value(i));
            text.append(": " + Utils.roundDouble(distribution.perClass(i), 2));
            if (i < numClasses - 1) {
                text.append("|");
            } else {
                text.append("}");
            }
        }

        return text.toString();
    }

    private int numInstancesIn(ClassifierTree node) {
        // Is there a better way to retrieve this information?
        var distribution = node.getLocalModel().distribution();
        var numInstances = 0;
        for (int i = 0; i < distribution.numBags(); i++) {
            numInstances += distribution.perBag(i);
        }
        return numInstances;
    }
}
