package de.hpi.bpt.logtransformer.transformation.operations.multi.data;

import de.hpi.bpt.logtransformer.transformation.datastructures.CaseColumn;
import de.hpi.bpt.logtransformer.transformation.datastructures.ColumnCaseLog;
import de.hpi.bpt.logtransformer.transformation.datastructures.ColumnEventLog;
import de.hpi.bpt.logtransformer.transformation.datastructures.LogColumn;
import de.hpi.bpt.logtransformer.transformation.operations.LogTransformation;

import java.util.*;

/**
 * Transformations D1_an to D6_an
 */
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
                    || sourceColumnName.equals(sourceSchema.getActivityName())
                    || sourceColumnName.equals(sourceSchema.getResourceName())) {
                continue;
            }

            var startColumnName = sourceColumnName + " (at start)";
            var endColumnName = sourceColumnName + " (at end)";
            var uniqueColumnName = sourceColumnName + " (#distinct values)";
            targetSchema.addColumnDefinition(startColumnName, sourceColumnType);
            targetSchema.addColumnDefinition(endColumnName, sourceColumnType);
            targetSchema.addColumnDefinition(uniqueColumnName, Integer.class);

            if (String.class.equals(sourceColumn.getType())) {
                var uniqueValues = sourceEventLog.getUniqueStringValues(sourceColumnName);
                if (uniqueValues.size() <= 10) {
                    for (String uniqueStringValue : uniqueValues) {
                        targetSchema.addColumnDefinition(sourceColumnName + " = '" + uniqueStringValue + "' (times present)", Integer.class);
                    }
                    resultCaseLog.putAll(transformStringColumn(sourceColumn.as(String.class), sourceColumnName, uniqueValues));
                } else {
                    resultCaseLog.putAll(transformColumn(sourceColumn, sourceColumnName));
                }
            } else if (Boolean.class.equals(sourceColumn.getType())) {
                targetSchema.addColumnDefinition(sourceColumnName + " = 'true' (times present)", Integer.class);
                targetSchema.addColumnDefinition(sourceColumnName + " = 'false' (times present)", Integer.class);
                resultCaseLog.putAll(transformBooleanColumn(sourceColumn.as(Boolean.class), sourceColumnName));
            } else if (Date.class.equals(sourceColumn.getType())) {
                targetSchema.addColumnDefinition(sourceColumnName + " (max)", Date.class);
                targetSchema.addColumnDefinition(sourceColumnName + " (min)", Date.class);
                targetSchema.addColumnDefinition(sourceColumnName + " (avg)", Double.class);
                resultCaseLog.putAll(transformDateColumn(sourceColumn.as(Date.class), sourceColumnName));
            } else if (Integer.class.equals(sourceColumn.getType())) {
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
        result.put(sourceColumnName + " (#distinct values)", uniqueColumn);
        result.put(sourceColumnName + " (max)", maxColumn);
        result.put(sourceColumnName + " (min)", minColumn);
        result.put(sourceColumnName + " (avg)", avgColumn);
        return result;
    }

    private Map<String, CaseColumn<?>> transformDateColumn(LogColumn<Date> logColumn, String sourceColumnName) {
        var result = new LinkedHashMap<String, CaseColumn<?>>();
        var startColumn = new CaseColumn<>(Date.class);
        var endColumn = new CaseColumn<>(Date.class);
        var uniqueColumn = new CaseColumn<>(Date.class);
        var maxColumn = new CaseColumn<>(Date.class);
        var minColumn = new CaseColumn<>(Date.class);

        for (var traceColumn : logColumn.getTraces()) {
            var firstValue = traceColumn.get(0);
            startColumn.addValue(firstValue);
            endColumn.addValue(traceColumn.get(traceColumn.size() - 1));
            uniqueColumn.addValue(new HashSet<>(traceColumn).size());
            var stats = traceColumn.stream().filter(Objects::nonNull).mapToLong(e -> e.toInstant().toEpochMilli()).summaryStatistics();
            maxColumn.addValue(new Date(stats.getMax()));
            minColumn.addValue(new Date(stats.getMin()));
        }

        result.put(sourceColumnName + " (at start)", startColumn);
        result.put(sourceColumnName + " (at end)", endColumn);
        result.put(sourceColumnName + " (#distinct values)", uniqueColumn);
        result.put(sourceColumnName + " (max)", maxColumn);
        result.put(sourceColumnName + " (min)", minColumn);
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
        result.put(sourceColumnName + " (#distinct values)", uniqueColumn);
        result.put(sourceColumnName + " (max)", maxColumn);
        result.put(sourceColumnName + " (min)", minColumn);
        result.put(sourceColumnName + " (avg)", avgColumn);
        return result;
    }

    private Map<String, CaseColumn<?>> transformStringColumn(LogColumn<String> logColumn, String sourceColumnName, List<String> uniqueValues) {
        var result = new LinkedHashMap<String, CaseColumn<?>>();
        var startColumn = new CaseColumn<>(String.class);
        var endColumn = new CaseColumn<>(String.class);
        var uniqueColumn = new CaseColumn<>(Integer.class);

        Map<String, CaseColumn<Integer>> valueColumns = new LinkedHashMap<>();
        uniqueValues.forEach(uniqueValue -> valueColumns.put(uniqueValue, new CaseColumn<>(Integer.class)));


        for (List<String> traceColumn : logColumn.getTraces()) {
            startColumn.addValue(traceColumn.get(0));
            endColumn.addValue(traceColumn.get(traceColumn.size() - 1));
            uniqueColumn.addValue(new HashSet<>(traceColumn).size());
            uniqueValues.forEach(uniqueValue -> valueColumns.get(uniqueValue).addValue((int) traceColumn.stream().filter(uniqueValue::equals).count()));
        }

        result.put(sourceColumnName + " (at start)", startColumn);
        result.put(sourceColumnName + " (at end)", endColumn);
        result.put(sourceColumnName + " (#distinct values)", uniqueColumn);
        valueColumns.forEach((value, caseColumn) -> result.put(sourceColumnName + " = '" + value + "' (times present)", caseColumn));
        return result;
    }

    private Map<String, CaseColumn<?>> transformBooleanColumn(LogColumn<Boolean> logColumn, String sourceColumnName) {
        var result = new LinkedHashMap<String, CaseColumn<?>>();
        var startColumn = new CaseColumn<>(Boolean.class);
        var endColumn = new CaseColumn<>(Boolean.class);
        var uniqueColumn = new CaseColumn<>(Integer.class);

        var containsTrueColumn = new CaseColumn<>(Integer.class);
        var containsFalseColumn = new CaseColumn<>(Integer.class);

        for (List<Boolean> traceColumn : logColumn.getTraces()) {
            startColumn.addValue(traceColumn.get(0));
            endColumn.addValue(traceColumn.get(traceColumn.size() - 1));
            uniqueColumn.addValue(new HashSet<>(traceColumn).size());
            containsTrueColumn.addValue((int) traceColumn.stream().filter(b -> b).count());
            containsFalseColumn.addValue((int) traceColumn.stream().filter(b -> !b).count());
        }

        result.put(sourceColumnName + " (at start)", startColumn);
        result.put(sourceColumnName + " (at end)", endColumn);
        result.put(sourceColumnName + " (#distinct values)", uniqueColumn);
        result.put(sourceColumnName + " = 'true' (times present)", containsTrueColumn);
        result.put(sourceColumnName + " = 'false' (times present)", containsFalseColumn);
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
        result.put(sourceColumnName + " (#distinct values)", uniqueColumn);
        return result;
    }
}
