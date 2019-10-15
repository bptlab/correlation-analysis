package de.hpi.bpt.evaluation.decisiontree;

import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.REPTree;
import weka.core.Attribute;
import weka.core.Drawable;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Set;

public class DecisionTreeClassifier {

    public J48 buildJ48Tree(Instances data) {
        try {
            var classifier = new J48();
            classifier.setMinNumObj(Math.min(100, data.size() / 100));
            classifier.setBinarySplits(true);
            classifier.buildClassifier(data);

            return classifier;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public REPTree buildREPTree(Instances data) {
        try {
            var classifier = new REPTree();
            classifier.buildClassifier(data);
            return classifier;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
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
