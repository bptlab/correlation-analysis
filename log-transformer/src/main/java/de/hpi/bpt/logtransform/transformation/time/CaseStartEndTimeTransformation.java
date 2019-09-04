package de.hpi.bpt.logtransform.transformation.time;

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

        var caseStartColumn = resultCaseLog.addColumn("casestart", Date.class);
        var caseStartHourColumn = resultCaseLog.addColumn("casestart_hour", Integer.class);
        var caseStartDayOfWeekColumn = resultCaseLog.addColumn("casestart_day_of_week", String.class);
        var caseStartDayOfMonthColumn = resultCaseLog.addColumn("casestart_day_of_month", Integer.class);
        var caseStartDayOfYearColumn = resultCaseLog.addColumn("casestart_day_of_year", Integer.class);
        var caseStartMonthColumn = resultCaseLog.addColumn("casestart_month", String.class);
        var caseStartYearColumn = resultCaseLog.addColumn("casestart_year", Integer.class);

        var caseEndColumn = resultCaseLog.addColumn("caseend", Date.class);
        var caseEndHourColumn = resultCaseLog.addColumn("caseend_hour", Integer.class);
        var caseEndDayOfWeekColumn = resultCaseLog.addColumn("caseend_day_of_week", String.class);
        var caseEndDayOfMonthColumn = resultCaseLog.addColumn("caseend_day_of_month", Integer.class);
        var caseEndDayOfYearColumn = resultCaseLog.addColumn("caseend_day_of_year", Integer.class);
        var caseEndMonthColumn = resultCaseLog.addColumn("caseend_month", String.class);
        var caseEndYearColumn = resultCaseLog.addColumn("caseend_year", Integer.class);

        for (var trace : timestampColumn.getTraces()) {
            var startDate = trace.get(0);
            var endDate = trace.get(trace.size() - 1);

            addAll(startDate, caseStartColumn, caseStartHourColumn, caseStartDayOfWeekColumn, caseStartDayOfMonthColumn, caseStartDayOfYearColumn, caseStartMonthColumn, caseStartYearColumn);
            addAll(endDate, caseEndColumn, caseEndHourColumn, caseEndDayOfWeekColumn, caseEndDayOfMonthColumn, caseEndDayOfYearColumn, caseEndMonthColumn, caseEndYearColumn);
        }
    }

    private void addAll(Date date, CaseColumn<Date> absoluteColumn, CaseColumn<Integer> hourColumn, CaseColumn<String> dayOfWeekColumn, CaseColumn<Integer> dayOfMonthColumn, CaseColumn<Integer> dayOfYearColumn, CaseColumn<String> monthColumn, CaseColumn<Integer> yearColumn) {
        var offsetDateTime = date.toInstant().atZone(ZoneId.systemDefault());
        absoluteColumn.addValue(date);
        hourColumn.addValue(offsetDateTime.getHour());
        dayOfWeekColumn.addValue(offsetDateTime.getDayOfWeek().toString());
        dayOfMonthColumn.addValue(offsetDateTime.getDayOfMonth());
        dayOfYearColumn.addValue(offsetDateTime.getDayOfYear());
        monthColumn.addValue(offsetDateTime.getMonth().toString());
        yearColumn.addValue(offsetDateTime.getYear());
    }
}
