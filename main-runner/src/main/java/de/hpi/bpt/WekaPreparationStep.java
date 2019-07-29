package de.hpi.bpt;

import de.hpi.bpt.datastructures.RowCaseLog;
import de.hpi.bpt.io.ArffCaseLogWriter;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;
import weka.filters.unsupervised.attribute.StringToNominal;

import java.util.stream.IntStream;

class WekaPreparationStep {
    static Instances retrieveData(RowCaseLog caseLog) {
        // TODO can this be done without String serialization?
        try {
            var caseLogAsString = new ArffCaseLogWriter().writeToString(caseLog);
            var caseLogData = TimeTracker.runTimed(() -> new DataLoader()
                    .loadData(caseLogAsString), "Reading ARFF string into Instances");

            return removeWithWrongClassValue(
                    removeWithEmptyAttributes(
                            convertStringToNominal(
                                    caseLogData
                            )));

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }

    }

    private static Instances convertStringToNominal(Instances data) throws Exception {
        var stringToNominal = new StringToNominal();
        stringToNominal.setAttributeRange("first-last");
        stringToNominal.setInputFormat(data);

        data = Filter.useFilter(data, stringToNominal);
        return data;
    }

    private static Instances removeWithEmptyAttributes(final Instances data) throws Exception {
        var toRemove = IntStream.range(0, data.numAttributes())
                .filter(i -> {
                    var attribute = data.attribute(i);
                    return attribute.isNominal() && attribute.numValues() == 0;
                })
                .toArray();
        if (toRemove.length > 0) {
            var remove = new Remove();
            remove.setAttributeIndicesArray(toRemove);
            remove.setInputFormat(data);
            return Filter.useFilter(data, remove);
        } else {
            return data;
        }
    }

    private static Instances removeWithWrongClassValue(Instances data) {
        data.setClass(data.attribute(Parameters.TARGET_VARIABLE));
        data.deleteWithMissingClass();

        var classIndex = data.classIndex();
        var targetValueIndex = data.instance(0).attribute(classIndex).indexOfValue(Parameters.TARGET_VALUE);

        data.removeIf(instance -> !(instance.value(classIndex) == targetValueIndex));
        return data;
    }
}
