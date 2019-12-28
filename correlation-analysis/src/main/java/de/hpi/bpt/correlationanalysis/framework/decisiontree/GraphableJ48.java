package de.hpi.bpt.correlationanalysis.framework.decisiontree;

import weka.classifiers.trees.J48;
import weka.classifiers.trees.j48.ClassifierTree;
import weka.core.Instances;

/**
 * Exposes the root node {@link ClassifierTree} object of the tree of the {@link J48} classifier, allowing
 * for a traversal of the tree, checking for pure leaves in {@link DecisionTreeClassifier#buildJ48Tree(Instances)}.
 *
 * Also, overrides the {@link J48#graph()} and {@link J48#toString()} methods
 * to provide a more detailed tree graph representation.
 * Graph printing is done by the {@link J48GraphPrinter}.
 * Rule printing is done by the {@link J48RulesByClassPrinter}.
 */
public class GraphableJ48 extends J48 {

    ClassifierTree getRoot() {
        return m_root;
    }

    @Override
    public String graph() throws Exception {
        return new J48GraphPrinter().graph(m_root);
    }

    @Override
    public String toString() {
        try {
            return new J48RulesByClassPrinter().toString(m_root);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
