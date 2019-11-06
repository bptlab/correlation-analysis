package de.hpi.bpt.evaluation.decisiontree;

import weka.classifiers.trees.J48;
import weka.classifiers.trees.j48.ClassifierTree;

public class TraversableJ48 extends J48 {

    public ClassifierTree getRoot() {
        return m_root;
    }

    @Override
    public String graph() throws Exception {
        return new J48Traverser().graph(m_root);
    }



}
