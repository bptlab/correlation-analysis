package de.hpi.bpt.logtransform.transformation.multi.time;

import de.hpi.bpt.logtransform.datastructures.CaseColumn;
import de.hpi.bpt.logtransform.datastructures.ColumnCaseLog;
import de.hpi.bpt.logtransform.datastructures.ColumnEventLog;
import de.hpi.bpt.logtransform.transformation.LogTransformation;

import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

public class StageStartEndTimeTransformation implements LogTransformation {

    private final Map<String, String> activityToStage = new HashMap<>();

    public StageStartEndTimeTransformation(Map<String, String> activityToStage) {
        this.activityToStage.putAll(activityToStage);
    }

    @Override
    public void transform(ColumnEventLog sourceEventLog, ColumnCaseLog resultCaseLog) {
        var stages = activityToStage.values().stream().distinct().sorted(String::compareToIgnoreCase).collect(toList());

        for (String stage : stages) {
            var caseStartHourColumn = resultCaseLog.addColumn(stage + " (first occurrence - hour)", Integer.class);
            var caseStartDayOfWeekColumn = resultCaseLog.addColumn(stage + " (first occurrence - day of week)", String.class);
            var caseStartDayOfMonthColumn = resultCaseLog.addColumn(stage + " (first occurrence - day of month)", Integer.class);
            var caseStartDayOfYearColumn = resultCaseLog.addColumn(stage + " (first occurrence - day of year)", Integer.class);
            var caseStartMonthColumn = resultCaseLog.addColumn(stage + " (first occurrence - month)", String.class);
            var caseStartYearColumn = resultCaseLog.addColumn(stage + " (first occurrence - year)", Integer.class);

            var caseEndHourColumn = resultCaseLog.addColumn(stage + " (last occurrence - hour)", Integer.class);
            var caseEndDayOfWeekColumn = resultCaseLog.addColumn(stage + " (last occurrence - day of week)", String.class);
            var caseEndDayOfMonthColumn = resultCaseLog.addColumn(stage + " (last occurrence - day of month)", Integer.class);
            var caseEndDayOfYearColumn = resultCaseLog.addColumn(stage + " (last occurrence - day of year)", Integer.class);
            var caseEndMonthColumn = resultCaseLog.addColumn(stage + " (last occurrence - month)", String.class);
            var caseEndYearColumn = resultCaseLog.addColumn(stage + " (last occurrence - year)", Integer.class);

            var timestampTraces = sourceEventLog.getTimestampColumn().getTraces();
            var activityTraces = sourceEventLog.getActivityColumn().getTraces();
            for (int i = 0; i < activityTraces.size(); i++) {
                List<String> stageTrace = activityTraces.get(i).stream().map(activity -> activityToStage.getOrDefault(activity, "NONE")).collect(toList());
                var firstIndex = stageTrace.indexOf(stage);
                var lastIndex = stageTrace.lastIndexOf(stage);
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
