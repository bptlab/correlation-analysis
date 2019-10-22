package de.hpi.bpt.logtransform.transformation.once.time;

import de.hpi.bpt.logtransform.datastructures.CaseColumn;
import de.hpi.bpt.logtransform.datastructures.ColumnCaseLog;
import de.hpi.bpt.logtransform.datastructures.ColumnEventLog;
import de.hpi.bpt.logtransform.transformation.LogTransformation;

import java.time.ZoneId;
import java.util.Date;

public class CaseStartEndTimeTransformation implements LogTransformation {

    @Override
    public void transform(ColumnEventLog sourceEventLog, ColumnCaseLog resultCaseLog) {
        var sourceSchema = sourceEventLog.getSchema();

        var timestampColumn = sourceEventLog.getTyped(sourceSchema.getTimestampName(), Date.class);

        var caseStartHourColumn = resultCaseLog.addColumn("Case start (hour)", Integer.class);
        var caseStartDayOfWeekColumn = resultCaseLog.addColumn("Case start (day of week)", String.class);
        var caseStartDayOfMonthColumn = resultCaseLog.addColumn("Case start (day of month)", Integer.class);
        var caseStartDayOfYearColumn = resultCaseLog.addColumn("Case start (day of year)", Integer.class);
        var caseStartMonthColumn = resultCaseLog.addColumn("Case start (month)", String.class);
        var caseStartYearColumn = resultCaseLog.addColumn("Case start (year)", Integer.class);

        var caseEndHourColumn = resultCaseLog.addColumn("Case end (hour)", Integer.class);
        var caseEndDayOfWeekColumn = resultCaseLog.addColumn("Case end (day of week)", String.class);
        var caseEndDayOfMonthColumn = resultCaseLog.addColumn("Case end (day of month)", Integer.class);
        var caseEndDayOfYearColumn = resultCaseLog.addColumn("Case end (day of year)", Integer.class);
        var caseEndMonthColumn = resultCaseLog.addColumn("Case end (month)", String.class);
        var caseEndYearColumn = resultCaseLog.addColumn("Case end (year)", Integer.class);

        for (var trace : timestampColumn.getTraces()) {
            var startDate = trace.get(0);
            var endDate = trace.get(trace.size() - 1);

            addAll(startDate, caseStartHourColumn, caseStartDayOfWeekColumn, caseStartDayOfMonthColumn, caseStartDayOfYearColumn, caseStartMonthColumn, caseStartYearColumn);
            addAll(endDate, caseEndHourColumn, caseEndDayOfWeekColumn, caseEndDayOfMonthColumn, caseEndDayOfYearColumn, caseEndMonthColumn, caseEndYearColumn);
        }
    }

    private void addAll(Date date, CaseColumn<Integer> hourColumn, CaseColumn<String> dayOfWeekColumn, CaseColumn<Integer> dayOfMonthColumn, CaseColumn<Integer> dayOfYearColumn, CaseColumn<String> monthColumn, CaseColumn<Integer> yearColumn) {
        var offsetDateTime = date.toInstant().atZone(ZoneId.systemDefault());
        hourColumn.addValue(offsetDateTime.getHour());
        dayOfWeekColumn.addValue(offsetDateTime.getDayOfWeek().toString());
        dayOfMonthColumn.addValue(offsetDateTime.getDayOfMonth());
        dayOfYearColumn.addValue(offsetDateTime.getDayOfYear());
        monthColumn.addValue(offsetDateTime.getMonth().toString());
        yearColumn.addValue(offsetDateTime.getYear());
    }
}
