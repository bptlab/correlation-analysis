package de.hpi.bpt;

import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import weka.core.Instances;

import java.io.File;
import java.io.IOException;

import static de.hpi.bpt.Parameters.FOLDER;
import static de.hpi.bpt.Parameters.GRAPH_OUTPUT_FILE;

class DecisionTreeStep {
    static void buildDecisionTree(Instances data) {
        try {
            var decisionTreeOutput = TimeTracker.runTimed(() -> new DecisionTreeClassifier().buildDecisionRules(data), "Learning decision tree");
            Graphviz.fromString(decisionTreeOutput).render(Format.PNG).toFile(new File(FOLDER + GRAPH_OUTPUT_FILE));
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
