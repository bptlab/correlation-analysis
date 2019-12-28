package de.hpi.bpt.correlationanalysis.framework.decisiontree;

import weka.classifiers.trees.j48.ClassifierTree;

class J48RulesByClassPrinter {

    /**
     * Taken from {@link ClassifierTree#toString()}.
     * Adapted to print the rules separated by possible class values, for readability.
     *
     * @param root Root node of the decision tree.
     * @return String representation of the decision tree rules, separated by class values.
     */
    String toString(ClassifierTree root) throws Exception {
        StringBuffer text = new StringBuffer();

        var trainingData = root.getTrainingData();
        for (int i = 0; i < trainingData.classAttribute().numValues(); i++) {
            var classValue = trainingData.classAttribute().value(i);
            text.append(classValue).append(": ").append("\n");

            if (root.isLeaf()) {
                if (i == root.getLocalModel().distribution().maxClass()) {
                    text.append(": ");
                    text.append(root.getLocalModel().dumpLabel(0, trainingData));
                }
            } else {
                dumpTree(root, 0, text, i);
            }
            text.append("\n\n\n");
        }

        return text.toString();
    }


    private void dumpTree(ClassifierTree node, int depth, StringBuffer text, int classValueIndex) throws Exception {
        var children = node.getSons();
        var localModel = node.getLocalModel();
        var train = node.getTrainingData();
        for (int i = 0; i < children.length; i++) {
            if (!children[i].isLeaf()) {
                text.append("\n");
                text.append("|   ".repeat(Math.max(0, depth)));

                text.append(localModel.leftSide(train));
                text.append(localModel.rightSide(i, train));

                dumpTree(children[i], depth + 1, text, classValueIndex);
            } else {
                if (classValueIndex == children[i].getLocalModel().distribution().maxClass()) {
                    text.append("\n");
                    text.append("|   ".repeat(Math.max(0, depth)));

                    text.append(localModel.leftSide(train));
                    text.append(localModel.rightSide(i, train));

                    text.append(": ");
                    text.append(localModel.dumpLabel(i, train));
                }
            }
        }
    }
}
