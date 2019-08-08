package de.hpi.bpt;

import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import weka.core.Instances;

import java.io.File;
import java.io.IOException;

class DecisionTreeStep {
    static void buildDecisionTree(Instances data) {
        try {
            var decisionTreeOutput = TimeTracker.runTimed(() -> new DecisionTreeClassifier().buildDecisionRules(data), "Learning decision tree");
            Graphviz.fromString(decisionTreeOutput).render(Format.PNG).toFile(new File(Parameters.FOLDER + Parameters.GRAPH_OUTPUT_FILE));
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }

//        TimeTracker.runTimed(() -> new DecisionTreeClassifier().buildStumpForEachAttribute(data), "Learning decision trees");

//        TimeTracker.runTimed(() -> new DecisionTreeClassifier().buildDecisionRulesBoosting(data), "Learning decision trees (boosting)");
//        TimeTracker.runTimed(() -> new DecisionTreeClassifier().buildDecisionRulesCostSensitive(data), "Learning decision trees (cost sensitive)");

    }
}
