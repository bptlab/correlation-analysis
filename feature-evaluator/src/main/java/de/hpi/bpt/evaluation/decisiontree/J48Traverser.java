package de.hpi.bpt.evaluation.decisiontree;

import org.apache.commons.lang3.tuple.Pair;
import weka.classifiers.trees.j48.ClassifierTree;
import weka.classifiers.trees.j48.Distribution;
import weka.core.Instances;
import weka.core.Utils;

import java.util.ArrayDeque;

class J48Traverser {

    String graph(ClassifierTree root) throws Exception {
        var stack = new ArrayDeque<Pair<ClassifierTree, Integer>>();

        StringBuffer text = new StringBuffer();
        text.append("digraph J48Tree {\n").append("node [shape=Mrecord];");

        var id = 0;

        graphRoot(root, text);
        if (!root.isLeaf()) {
            var localModel = root.getLocalModel();
            var trainingData = root.getTrainingData();
            var children = root.getSons();
            for (int i1 = 0; i1 < children.length; i1++) {
                id++;
                text.append("N").append(0).append("->").append("N").append(id).append(" [label=\"").append(Utils.backQuoteChars(localModel.rightSide(i1, trainingData).trim())).append("\"]\n");
                if (children[i1].isLeaf()) {
                    text.append("N").append(id).append(" [label=\"").append(Utils.backQuoteChars(localModel.dumpLabel(i1, trainingData))).append("\" ").append("shape=box style=filled ");
                    text.append("]\n");
                } else {
                    text.append("N").append(id).append(" [label=\"{").append(Utils.backQuoteChars(children[i1].getLocalModel().leftSide(trainingData)))
                            .append("|").append(Utils.backQuoteChars(createSplitNodeLabel(children[i1], trainingData)))
                            .append("}\" ");
                    text.append("]\n");
                    stack.addFirst(Pair.of(children[i1], id));
                }
            }
        }

        while (!stack.isEmpty()) {
            var nodeAndId = stack.removeFirst();
            var node = nodeAndId.getLeft();
            var nodeId = nodeAndId.getRight();
            if (!node.isLeaf()) {
                var localModel = node.getLocalModel();
                var trainingData = node.getTrainingData();
                var children = node.getSons();
                for (int i = 0; i < children.length; i++) {
                    id++;
                    text.append("N").append(nodeId).append("->").append("N").append(id).append(" [label=\"").append(Utils.backQuoteChars(localModel.rightSide(i, trainingData).trim())).append("\"]\n");
                    if (children[i].isLeaf()) {
                        text.append("N").append(id).append(" [label=\"").append(Utils.backQuoteChars(localModel.dumpLabel(i, trainingData))).append("\" ").append("shape=box style=filled ");
                        text.append("]\n");
                    } else {
                        text.append("N").append(id).append(" [label=\"{").append(Utils.backQuoteChars(children[i].getLocalModel().leftSide(trainingData)))
                                .append("|").append(Utils.backQuoteChars(createSplitNodeLabel(children[i], trainingData)))
                                .append("}\" ");
                        text.append("]\n");
                        stack.addFirst(Pair.of(children[i], id));
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
            text.append("N").append(0)
                    .append(" [label=\"").append(Utils.backQuoteChars(root.getLocalModel().dumpLabel(0, trainingData))).append("\" ").append("shape=box style=filled ");
            text.append("]\n");
        } else {
            text.append("N").append(0).append(" [label=\"{").append(Utils.backQuoteChars(root.getLocalModel().leftSide(trainingData))).append("|")
                    .append(Utils.backQuoteChars(createSplitNodeLabel(root, trainingData))).append("}\" ");
            text.append("]\n");
        }
    }

    private String createSplitNodeLabel(ClassifierTree node, Instances data) {

        StringBuffer text;

        text = new StringBuffer();
        var numClasses = node.getLocalModel().distribution().numClasses();

        text.append("{");
        for (int i = 0; i < numClasses; i++) {
            Distribution distribution = node.getLocalModel().distribution();
            text.append(data.classAttribute().value(i));
            text.append(": " + Utils.roundDouble(distribution.perClass(i), 2));
            if (i < numClasses - 1) {
                text.append("|");
            } else {
                text.append("}");
            }
        }

        return text.toString();
    }
}
