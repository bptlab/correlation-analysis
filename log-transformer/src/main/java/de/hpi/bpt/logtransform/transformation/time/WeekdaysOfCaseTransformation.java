package de.hpi.bpt.logtransform.transformation.time;

import de.hpi.bpt.logtransform.datastructures.CaseColumn;
import de.hpi.bpt.logtransform.datastructures.ColumnCaseLog;
import de.hpi.bpt.logtransform.datastructures.ColumnEventLog;
import de.hpi.bpt.logtransform.transformation.LogTransformation;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class WeekdaysOfCaseTransformation implements LogTransformation {

    @Override
    public void transform(ColumnEventLog sourceEventLog, ColumnCaseLog resultCaseLog) {
        var sourceSchema = sourceEventLog.getSchema();
        var targetSchema = resultCaseLog.getSchema();
        var timestampColumn = sourceEventLog.getTyped(sourceSchema.getTimestampName(), Date.class);

        targetSchema.addColumnDefinition("casestartweekday", String.class);
        var startWeekDayColumn = new CaseColumn<>(String.class);

        targetSchema.addColumnDefinition("caseendweekday", String.class);
        var endWeekDayColumn = new CaseColumn<>(String.class);

        var dayInWeekFormat = new SimpleDateFormat("EEEE");
        for (List<Date> trace : timestampColumn.getTraces()) {
            startWeekDayColumn.addValue(dayInWeekFormat.format(trace.get(0)));
            endWeekDayColumn.addValue(dayInWeekFormat.format(trace.get(trace.size() - 1)));
        }

        resultCaseLog.put("casestartweekday", startWeekDayColumn);
        resultCaseLog.put("caseendweekday", endWeekDayColumn);
    }
}
