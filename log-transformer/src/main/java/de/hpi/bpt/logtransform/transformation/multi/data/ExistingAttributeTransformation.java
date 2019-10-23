package de.hpi.bpt.logtransform.transformation.multi.data;

import de.hpi.bpt.logtransform.datastructures.CaseColumn;
import de.hpi.bpt.logtransform.datastructures.ColumnCaseLog;
import de.hpi.bpt.logtransform.datastructures.ColumnEventLog;
import de.hpi.bpt.logtransform.datastructures.LogColumn;
import de.hpi.bpt.logtransform.transformation.LogTransformation;

import java.util.*;

public class ExistingAttributeTransformation implements LogTransformation {

    @Override
    public void transform(ColumnEventLog sourceEventLog, ColumnCaseLog resultCaseLog) {
        var sourceSchema = sourceEventLog.getSchema();
        var targetSchema = resultCaseLog.getSchema();

        for (var columnEntry : sourceEventLog.entrySet()) {
            var sourceColumn = columnEntry.getValue();
            var sourceColumnName = columnEntry.getKey();
            var sourceColumnType = sourceSchema.get(sourceColumnName).getType();

            if (sourceColumnName.equals(sourceSchema.getCaseIdName())
                    || sourceColumnName.equals(sourceSchema.getTimestampName())
                    || sourceColumnName.equals(sourceSchema.getActivityName())) {
                continue;
            }

            var startColumnName = sourceColumnName + " (at start)";
            var endColumnName = sourceColumnName + " (at end)";
            var uniqueColumnName = sourceColumnName + " (#unique values)";
            targetSchema.addColumnDefinition(startColumnName, sourceColumnType);
            targetSchema.addColumnDefinition(endColumnName, sourceColumnType);
            targetSchema.addColumnDefinition(uniqueColumnName, Integer.class);

            if (Integer.class.equals(sourceColumn.getType())) {
                targetSchema.addColumnDefinition(sourceColumnName + " (max)", Integer.class);
                targetSchema.addColumnDefinition(sourceColumnName + " (min)", Integer.class);
                targetSchema.addColumnDefinition(sourceColumnName + " (avg)", Double.class);
                resultCaseLog.putAll(transformIntegerColumn(sourceColumn.as(Integer.class), sourceColumnName));
            } else if (Double.class.equals(sourceColumn.getType())) {
                targetSchema.addColumnDefinition(sourceColumnName + " (max)", Double.class);
                targetSchema.addColumnDefinition(sourceColumnName + " (min)", Double.class);
                targetSchema.addColumnDefinition(sourceColumnName + " (avg)", Double.class);
                resultCaseLog.putAll(transformDoubleColumn(sourceColumn.as(Double.class), sourceColumnName));
            } else {
                resultCaseLog.putAll(transformColumn(sourceColumn, sourceColumnName));
            }
        }
    }

    private Map<String, CaseColumn<?>> transformIntegerColumn(LogColumn<Integer> logColumn, String sourceColumnName) {
        var result = new LinkedHashMap<String, CaseColumn<?>>();
        var startColumn = new CaseColumn<>(Integer.class);
        var endColumn = new CaseColumn<>(Integer.class);
        var uniqueColumn = new CaseColumn<>(Integer.class);
        var maxColumn = new CaseColumn<>(Integer.class);
        var minColumn = new CaseColumn<>(Integer.class);
        var avgColumn = new CaseColumn<>(Double.class);

        for (List<Integer> traceColumn : logColumn.getTraces()) {
            var firstValue = traceColumn.get(0);
            startColumn.addValue(firstValue);
            endColumn.addValue(traceColumn.get(traceColumn.size() - 1));
            uniqueColumn.addValue(new HashSet<>(traceColumn).size());
            var stats = traceColumn.stream().filter(Objects::nonNull).mapToInt(e -> e).summaryStatistics();
            maxColumn.addValue(stats.getMax());
            minColumn.addValue(stats.getMin());
            avgColumn.addValue(stats.getAverage());
        }

        result.put(sourceColumnName + " (at start)", startColumn);
        result.put(sourceColumnName + " (at end)", endColumn);
        result.put(sourceColumnName + " (#unique values)", uniqueColumn);
        result.put(sourceColumnName + " (max)", maxColumn);
        result.put(sourceColumnName + " (min)", minColumn);
        result.put(sourceColumnName + " (avg)", avgColumn);
        return result;
    }

    private Map<String, CaseColumn<?>> transformDoubleColumn(LogColumn<Double> logColumn, String sourceColumnName) {
        var result = new LinkedHashMap<String, CaseColumn<?>>();
        var startColumn = new CaseColumn<>(Double.class);
        var endColumn = new CaseColumn<>(Double.class);
        var uniqueColumn = new CaseColumn<>(Integer.class);
        var maxColumn = new CaseColumn<>(Double.class);
        var minColumn = new CaseColumn<>(Double.class);
        var avgColumn = new CaseColumn<>(Double.class);

        for (List<Double> traceColumn : logColumn.getTraces()) {
            var firstValue = traceColumn.get(0);
            startColumn.addValue(firstValue);
            endColumn.addValue(traceColumn.get(traceColumn.size() - 1));
            uniqueColumn.addValue(new HashSet<>(traceColumn).size());
            var stats = traceColumn.stream().filter(Objects::nonNull).mapToDouble(e -> e).summaryStatistics();
            maxColumn.addValue(stats.getMax());
            minColumn.addValue(stats.getMin());
            avgColumn.addValue(stats.getAverage());
        }

        result.put(sourceColumnName + " (at start)", startColumn);
        result.put(sourceColumnName + " (at end)", endColumn);
        result.put(sourceColumnName + " (#unique values)", uniqueColumn);
        result.put(sourceColumnName + " (max)", maxColumn);
        result.put(sourceColumnName + " (min)", minColumn);
        result.put(sourceColumnName + " (avg)", avgColumn);
        return result;
    }

    private <T> Map<String, CaseColumn<?>> transformColumn(LogColumn<T> logColumn, String sourceColumnName) {
        var result = new LinkedHashMap<String, CaseColumn<?>>();
        var startColumn = new CaseColumn<>(logColumn.getType());
        var endColumn = new CaseColumn<>(logColumn.getType());
        var uniqueColumn = new CaseColumn<>(Integer.class);

        for (List<T> traceColumn : logColumn.getTraces()) {
            startColumn.addValue(traceColumn.get(0));
            endColumn.addValue(traceColumn.get(traceColumn.size() - 1));
            uniqueColumn.addValue(new HashSet<>(traceColumn).size());
        }

        result.put(sourceColumnName + " (at start)", startColumn);
        result.put(sourceColumnName + " (at end)", endColumn);
        result.put(sourceColumnName + " (#unique values)", uniqueColumn);
        return result;
    }
}
