package de.hpi.bpt.logtransformer.transformation.transformation.multi.resource;

import de.hpi.bpt.logtransformer.transformation.datastructures.ColumnCaseLog;
import de.hpi.bpt.logtransformer.transformation.datastructures.ColumnEventLog;
import de.hpi.bpt.logtransformer.transformation.transformation.LogTransformation;

public class TimesResourceInvolvedTransformation implements LogTransformation {

    @Override
    public void transform(ColumnEventLog sourceEventLog, ColumnCaseLog resultCaseLog) {
        var sourceSchema = sourceEventLog.getSchema();
        if (sourceSchema.getResourceName() == null) {
            throw new RuntimeException("Cannot do resource based transformation: resource information is missing!");
        }

        var resourceColumn = sourceEventLog.getResourceColumn();

        for (var resource : sourceEventLog.getUniqueResourceNames()) {
            var timesInvolvedColumn = resultCaseLog.addColumn(
                    String.format("#Resource '%s' involved", resource),
                    Integer.class
            );

            for (var trace : resourceColumn.getTraces()) {
                timesInvolvedColumn.addValue((int) trace.stream().filter(resource::equals).count());
            }
        }
    }

}
