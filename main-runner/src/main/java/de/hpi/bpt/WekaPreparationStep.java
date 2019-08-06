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

            var preprocessedData = applyFilters(caseLogData);

            preprocessedData.setClass(preprocessedData.attribute(Parameters.TARGET_VARIABLE));
            preprocessedData.deleteWithMissingClass();
            return preprocessedData;

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }

    }

    public static Instances removeWithWrongClassValue(Instances data) {
        var newData = new Instances(data);
        newData.setClass(data.classAttribute());

        var classIndex = newData.classIndex();
        var targetValueIndex = newData.instance(0).attribute(classIndex).indexOfValue(Parameters.TARGET_VALUE);

        newData.removeIf(instance -> !(instance.value(classIndex) == targetValueIndex));
        return newData;
    }

    private static Instances applyFilters(Instances data) throws Exception {
        var nominal = Filter.useFilter(data, stringToNominalFilter(data));
        return Filter.useFilter(nominal, removeEmptyAttributesFilter(nominal));
    }

    private static StringToNominal stringToNominalFilter(Instances data) throws Exception {
        var stringToNominal = new StringToNominal();
        stringToNominal.setAttributeRange("first-last");
        stringToNominal.setInputFormat(data);
        return stringToNominal;
    }

    private static Remove removeEmptyAttributesFilter(final Instances data) throws Exception {
        var toRemove = IntStream.range(0, data.numAttributes())
                .filter(i -> {
                    var attribute = data.attribute(i);
                    return attribute.isNominal() && attribute.numValues() == 0;
                })
                .toArray();
        var remove = new Remove();
        remove.setAttributeIndicesArray(toRemove);
        remove.setInputFormat(data);
        return remove;
    }
}
