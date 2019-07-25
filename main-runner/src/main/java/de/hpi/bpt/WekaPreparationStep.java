package de.hpi.bpt;

import de.hpi.bpt.datastructures.RowCaseLog;
import de.hpi.bpt.io.ArffCaseLogWriter;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.NumericToNominal;
import weka.filters.unsupervised.attribute.StringToNominal;

class WekaPreparationStep {
    static Instances retrieveData(RowCaseLog caseLog) {
        // TODO can this be done without String serialization?
        try {
            var caseLogAsString = new ArffCaseLogWriter().writeToString(caseLog);
            var data = TimeTracker.runTimed(() -> new DataLoader().ignoring(Parameters.CASE_ID_NAME).loadData(caseLogAsString), "Reading ARFF string into Instances");
            data.setClass(data.attribute(Parameters.TARGET_VARIABLE));

            var numericToNominal = new NumericToNominal();
            numericToNominal.setInputFormat(data);

            var stringToNominal = new StringToNominal();
            stringToNominal.setInputFormat(data);

            data = Filter.useFilter(
                    Filter.useFilter(data, stringToNominal),
                    numericToNominal
            );

            data.removeIf(instance -> Double.isNaN(instance.classValue()));

            return data;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }

    }
}
