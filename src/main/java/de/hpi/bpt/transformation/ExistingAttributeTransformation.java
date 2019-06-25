package de.hpi.bpt.transformation;

import de.hpi.bpt.datastructures.CaseLog;
import de.hpi.bpt.datastructures.EventLog;
import de.hpi.bpt.datastructures.Schema;
import de.hpi.bpt.datastructures.CaseColumn;
import de.hpi.bpt.datastructures.LogColumn;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ExistingAttributeTransformation {

    private EventLog sourceEventLog;

    public ExistingAttributeTransformation(EventLog sourceEventLog) {
        this.sourceEventLog = sourceEventLog;
    }

    public CaseLog transform() {
        Schema sourceSchema = sourceEventLog.getSchema();
        Schema targetSchema = new Schema();
        Map<String, CaseColumn<?>> transformedColumns = new LinkedHashMap<>();

        for (var columnEntry : sourceEventLog.entrySet()) {
            var sourceColumn = columnEntry.getValue();
            var sourceColumnName = columnEntry.getKey();
            var sourceColumnType = sourceSchema.get(sourceColumnName).getType();

            var startColumnName = sourceColumnName + "_start";
            var endColumnName = sourceColumnName + "_end";
            targetSchema.addColumnDefinition(startColumnName, sourceColumnType);
            targetSchema.addColumnDefinition(endColumnName, sourceColumnType);

            if (Integer.class.equals(sourceColumn.getType())) {
                targetSchema.addColumnDefinition(sourceColumnName + "_max", Integer.class);
                targetSchema.addColumnDefinition(sourceColumnName + "_max", Integer.class);
                targetSchema.addColumnDefinition(sourceColumnName + "_avg", Double.class);
                transformedColumns.putAll(transformIntegerColumn(sourceColumn.as(Integer.class), sourceColumnName));
            } else if (Double.class.equals(sourceColumn.getType())) {
                targetSchema.addColumnDefinition(sourceColumnName + "_max", Double.class);
                targetSchema.addColumnDefinition(sourceColumnName + "_max", Double.class);
                targetSchema.addColumnDefinition(sourceColumnName + "_avg", Double.class);
                transformedColumns.putAll(transformDoubleColumn(sourceColumn.as(Double.class), sourceColumnName));
            } else {
                transformedColumns.putAll(transformColumn(sourceColumn, sourceColumnName));
            }
        }

        return new CaseLog(targetSchema, transformedColumns);
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
            var stats = traceColumn.stream().mapToInt(e -> e).summaryStatistics();
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
            var stats = traceColumn.stream().mapToDouble(e -> e).summaryStatistics();
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
