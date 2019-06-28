package de.hpi.bpt.transformation;

import de.hpi.bpt.datastructures.CaseColumn;
import de.hpi.bpt.datastructures.EventLog;
import de.hpi.bpt.datastructures.LogColumn;
import de.hpi.bpt.datastructures.Schema;

import java.time.Duration;
import java.util.*;

public class ExistingAttributeTransformation implements Transformation {

    @Override
    public void transform(EventLog sourceEventLog, Schema targetSchema, Map<String, CaseColumn<?>> transformedColumns) {
        var sourceSchema = sourceEventLog.getSchema();

        for (var columnEntry : sourceEventLog.entrySet()) {
            var sourceColumn = columnEntry.getValue();
            var sourceColumnName = columnEntry.getKey();
            var sourceColumnType = sourceSchema.get(sourceColumnName).getType();

            if (sourceColumnName.equals(sourceSchema.getCaseIdName())) {
                targetSchema.addColumnDefinition(sourceColumnName, sourceColumnType);
                transformedColumns.put(sourceColumnName, transformCaseIdColumn(sourceColumn.as(String.class)));
                continue;
            }

            if (sourceColumnName.equals(sourceSchema.getTimestampName())) {
                targetSchema.addColumnDefinition("duration", Integer.class);
                transformedColumns.put("duration", transformTimestampColumnToDuration(sourceColumn.as(Date.class)));
                continue;
            }

            var startColumnName = sourceColumnName + "_start";
            var endColumnName = sourceColumnName + "_end";
            targetSchema.addColumnDefinition(startColumnName, sourceColumnType);
            targetSchema.addColumnDefinition(endColumnName, sourceColumnType);

            if (Integer.class.equals(sourceColumn.getType())) {
                targetSchema.addColumnDefinition(sourceColumnName + "_max", Integer.class);
                targetSchema.addColumnDefinition(sourceColumnName + "_min", Integer.class);
                targetSchema.addColumnDefinition(sourceColumnName + "_avg", Double.class);
                transformedColumns.putAll(transformIntegerColumn(sourceColumn.as(Integer.class), sourceColumnName));
            } else if (Double.class.equals(sourceColumn.getType())) {
                targetSchema.addColumnDefinition(sourceColumnName + "_max", Double.class);
                targetSchema.addColumnDefinition(sourceColumnName + "_min", Double.class);
                targetSchema.addColumnDefinition(sourceColumnName + "_avg", Double.class);
                transformedColumns.putAll(transformDoubleColumn(sourceColumn.as(Double.class), sourceColumnName));
            } else {
                transformedColumns.putAll(transformColumn(sourceColumn, sourceColumnName));
            }
        }
    }

    private CaseColumn<String> transformCaseIdColumn(LogColumn<String> caseIdColumn) {
        var column = new CaseColumn<>(String.class);
        for (List<String> trace : caseIdColumn.getTraces()) {
            column.addValue(trace.get(0)); // simply add first value, assuming the case id is the same for the one trace
        }
        return column;
    }


    private CaseColumn<Integer> transformTimestampColumnToDuration(LogColumn<Date> timestampColumn) {
        var column = new CaseColumn<>(Integer.class);
        for (List<Date> trace : timestampColumn.getTraces()) {
            var duration = Duration.between(trace.get(0).toInstant(), trace.get(trace.size() - 1).toInstant());
            column.addValue((int) duration.getSeconds());
        }
        return column;
    }

    private Map<String, CaseColumn<?>> transformIntegerColumn(LogColumn<Integer> logColumn, String sourceColumnName) {
        var result = new LinkedHashMap<String, CaseColumn<?>>();
        var startColumn = new CaseColumn<>(Integer.class);
        var endColumn = new CaseColumn<>(Integer.class);
        var maxColumn = new CaseColumn<>(Integer.class);
        var minColumn = new CaseColumn<>(Integer.class);
        var avgColumn = new CaseColumn<>(Double.class);

        for (List<Integer> traceColumn : logColumn.getTraces()) {
            var firstValue = traceColumn.get(0);
            startColumn.addValue(firstValue);
            endColumn.addValue(traceColumn.get(traceColumn.size() - 1));
            var stats = traceColumn.stream().filter(Objects::nonNull).mapToInt(e -> e).summaryStatistics();
            maxColumn.addValue(stats.getMax());
            minColumn.addValue(stats.getMin());
            avgColumn.addValue(stats.getAverage());
        }

        result.put(sourceColumnName + "_start", startColumn);
        result.put(sourceColumnName + "_end", endColumn);
        result.put(sourceColumnName + "_max", maxColumn);
        result.put(sourceColumnName + "_min", minColumn);
        result.put(sourceColumnName + "_avg", avgColumn);
        return result;
    }

    private Map<String, CaseColumn<?>> transformDoubleColumn(LogColumn<Double> logColumn, String sourceColumnName) {
        var result = new LinkedHashMap<String, CaseColumn<?>>();
        var startColumn = new CaseColumn<>(Double.class);
        var endColumn = new CaseColumn<>(Double.class);
        var maxColumn = new CaseColumn<>(Double.class);
        var minColumn = new CaseColumn<>(Double.class);
        var avgColumn = new CaseColumn<>(Double.class);

        for (List<Double> traceColumn : logColumn.getTraces()) {
            var firstValue = traceColumn.get(0);
            startColumn.addValue(firstValue);
            endColumn.addValue(traceColumn.get(traceColumn.size() - 1));
            var stats = traceColumn.stream().filter(Objects::nonNull).mapToDouble(e -> e).summaryStatistics();
            maxColumn.addValue(stats.getMax());
            minColumn.addValue(stats.getMin());
            avgColumn.addValue(stats.getAverage());
        }

        result.put(sourceColumnName + "_start", startColumn);
        result.put(sourceColumnName + "_end", endColumn);
        result.put(sourceColumnName + "_max", maxColumn);
        result.put(sourceColumnName + "_min", minColumn);
        result.put(sourceColumnName + "_avg", avgColumn);
        return result;
    }

    private <T> Map<String, CaseColumn<T>> transformColumn(LogColumn<T> logColumn, String sourceColumnName) {
        var result = new LinkedHashMap<String, CaseColumn<T>>();
        var startColumn = new CaseColumn<>(logColumn.getType());
        var endColumn = new CaseColumn<>(logColumn.getType());

        for (List<T> traceColumn : logColumn.getTraces()) {
            startColumn.addValue(traceColumn.get(0));
            endColumn.addValue(traceColumn.get(traceColumn.size() - 1));
        }

        result.put(sourceColumnName + "_start", startColumn);
        result.put(sourceColumnName + "_end", endColumn);
        return result;
    }
}
