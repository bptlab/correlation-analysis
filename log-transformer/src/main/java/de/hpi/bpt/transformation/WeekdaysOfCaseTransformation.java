package de.hpi.bpt.transformation;

import de.hpi.bpt.datastructures.CaseColumn;
import de.hpi.bpt.datastructures.CaseLog;
import de.hpi.bpt.datastructures.EventLog;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class WeekdaysOfCaseTransformation implements LogTransformation {

    @Override
    public void transform(EventLog sourceEventLog, CaseLog resultCaseLog) {
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
