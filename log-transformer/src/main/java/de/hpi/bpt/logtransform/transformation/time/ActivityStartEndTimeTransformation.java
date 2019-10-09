package de.hpi.bpt.logtransform.transformation.time;

import de.hpi.bpt.logtransform.datastructures.CaseColumn;
import de.hpi.bpt.logtransform.datastructures.ColumnCaseLog;
import de.hpi.bpt.logtransform.datastructures.ColumnEventLog;
import de.hpi.bpt.logtransform.transformation.LogTransformation;

import java.time.ZoneId;
import java.util.Date;
import java.util.List;

public class ActivityStartEndTimeTransformation implements LogTransformation {

    @Override
    public void transform(ColumnEventLog sourceEventLog, ColumnCaseLog resultCaseLog) {
        for (String activity : sourceEventLog.getUniqueActivityNames()) {
            var caseStartHourColumn = resultCaseLog.addColumn(activity + "_first_hour", Integer.class);
            var caseStartDayOfWeekColumn = resultCaseLog.addColumn(activity + "_first_day_of_week", String.class);
            var caseStartDayOfMonthColumn = resultCaseLog.addColumn(activity + "_first_day_of_month", Integer.class);
            var caseStartDayOfYearColumn = resultCaseLog.addColumn(activity + "_first_day_of_year", Integer.class);
            var caseStartMonthColumn = resultCaseLog.addColumn(activity + "_first_month", String.class);
            var caseStartYearColumn = resultCaseLog.addColumn(activity + "_first_year", Integer.class);

            var caseEndHourColumn = resultCaseLog.addColumn(activity + "_last_hour", Integer.class);
            var caseEndDayOfWeekColumn = resultCaseLog.addColumn(activity + "_last_day_of_week", String.class);
            var caseEndDayOfMonthColumn = resultCaseLog.addColumn(activity + "_last_day_of_month", Integer.class);
            var caseEndDayOfYearColumn = resultCaseLog.addColumn(activity + "_last_day_of_year", Integer.class);
            var caseEndMonthColumn = resultCaseLog.addColumn(activity + "_last_month", String.class);
            var caseEndYearColumn = resultCaseLog.addColumn(activity + "_last_year", Integer.class);

            var timestampTraces = sourceEventLog.getTimestampColumn().getTraces();
            var activityTraces = sourceEventLog.getActivityColumn().getTraces();
            for (int i = 0; i < activityTraces.size(); i++) {
                List<String> activityTrace = activityTraces.get(i);
                var firstIndex = activityTrace.indexOf(activity);
                var lastIndex = activityTrace.lastIndexOf(activity);
                if (firstIndex >= 0) {
                    addAll(timestampTraces.get(i).get(firstIndex), caseStartHourColumn, caseStartDayOfWeekColumn, caseStartDayOfMonthColumn, caseStartDayOfYearColumn, caseStartMonthColumn, caseStartYearColumn);
                    addAll(timestampTraces.get(i).get(lastIndex), caseEndHourColumn, caseEndDayOfWeekColumn, caseEndDayOfMonthColumn, caseEndDayOfYearColumn, caseEndMonthColumn, caseEndYearColumn);
                } else {
                    addNull(caseStartHourColumn, caseStartDayOfWeekColumn, caseStartDayOfMonthColumn, caseStartDayOfYearColumn, caseStartMonthColumn, caseStartYearColumn);
                    addNull(caseEndHourColumn, caseEndDayOfWeekColumn, caseEndDayOfMonthColumn, caseEndDayOfYearColumn, caseEndMonthColumn, caseEndYearColumn);
                }
            }
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

    private void addNull(CaseColumn<Integer> hourColumn, CaseColumn<String> dayOfWeekColumn, CaseColumn<Integer> dayOfMonthColumn, CaseColumn<Integer> dayOfYearColumn, CaseColumn<String> monthColumn, CaseColumn<Integer> yearColumn) {
        hourColumn.addValue(null);
        dayOfWeekColumn.addValue(null);
        dayOfMonthColumn.addValue(null);
        dayOfYearColumn.addValue(null);
        monthColumn.addValue(null);
        yearColumn.addValue(null);
    }
}
