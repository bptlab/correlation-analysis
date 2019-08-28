package de.hpi.bpt.logtransform.io;

import de.hpi.bpt.logtransform.datastructures.CaseColumn;
import de.hpi.bpt.logtransform.datastructures.ColumnCaseLog;
import de.hpi.bpt.logtransform.datastructures.RowCaseLog;
import de.hpi.bpt.logtransform.datastructures.Schema;
import org.supercsv.io.CsvMapReader;
import org.supercsv.io.ICsvMapReader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public class CsvCaseLogReader {

    private CsvLogReader basicReader;

    public CsvCaseLogReader() {
        basicReader = new CsvLogReader();
    }

    public CsvCaseLogReader(CsvLogReader basicReader) {
        this.basicReader = basicReader;
    }

    public ColumnCaseLog readToColumnCaseLog(File file, String logName) {
        try (var mapReader = basicReader.read(file)) {
            var header = mapReader.getHeader(false);
            var schema = basicReader.readSchemaFromHeader(header);
            var columns = readColumns(header, schema, mapReader);
            return new ColumnCaseLog(logName, schema, columns);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public RowCaseLog readToRowCaseLog(File file, String logName) {
        try (var mapReader = basicReader.read(file)) {
            var header = mapReader.getHeader(false);
            var schema = basicReader.readSchemaFromHeader(header);
            var rowCaseLog = new RowCaseLog(logName, schema);
            readRows(header, schema, mapReader, rowCaseLog);
            return rowCaseLog;
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public RowCaseLog readVariableToRowCaseLog(File file, String variableName) {
        try (var mapReader = basicReader.read(file)) {
            var header = mapReader.getHeader(false);
            var schema = basicReader.readSchemaFromHeader(header);
            return readVariableRows(header, schema, mapReader, variableName);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private RowCaseLog readVariableRows(String[] header, Schema schema, CsvMapReader reader, String variableName) throws IOException {
        var processors = basicReader.getProcessors(schema);
        var caseIdColumnName = header[schema.get(schema.getCaseIdName()).getId()];
        var variableColumnName = header[schema.get(variableName).getId()];
        schema.entrySet().removeIf(entry -> !entry.getKey().equals(basicReader.getCaseIdName()) && !entry.getKey().equals(variableName));
        var resultSchema = new Schema();
        schema.forEach((name, columnDefinition) -> resultSchema.addColumnDefinition(name, columnDefinition.getType()));
        resultSchema.setCaseIdName(schema.getCaseIdName());
        var rowCaseLog = new RowCaseLog(variableName + "_Log", resultSchema);


        Map<String, Object> rowMap;
        while ((rowMap = reader.read(header, processors)) != null) {
            var caseId = (String) rowMap.get(caseIdColumnName);
            var row = new ArrayList<>(Arrays.asList(caseId, rowMap.get(variableColumnName)));
            rowCaseLog.put(caseId, row);
        }

        return rowCaseLog;
    }

    private void readRows(String[] header, Schema schema, ICsvMapReader reader, RowCaseLog rowCaseLog) throws IOException {
        var processors = basicReader.getProcessors(schema);
        var caseIdColumnName = header[schema.get(schema.getCaseIdName()).getId()];


        Map<String, Object> rowMap;
        while ((rowMap = reader.read(header, processors)) != null) {
            var row = new ArrayList<>();
            for (String s : header) {
                row.add(rowMap.get(s));
            }

            var caseId = (String) rowMap.get(caseIdColumnName);
            rowCaseLog.put(caseId, row);
        }
    }

    private Map<String, CaseColumn<?>> readColumns(String[] header, Schema schema, ICsvMapReader reader) throws IOException {
        var columns = new LinkedHashMap<String, CaseColumn<?>>();
        for (var entry : schema.entrySet()) {
            columns.put(entry.getKey(), new CaseColumn<>(entry.getValue().getType()));
        }
        var columnArray = columns.keySet().toArray(new String[0]);

        var processors = basicReader.getProcessors(schema);

        Map<String, Object> rowMap;
        while ((rowMap = reader.read(header, processors)) != null) {
            for (int i = 0; i < header.length; i++) {
                var typedHeader = header[i];
                var untypedHeader = columnArray[i];
                columns.get(untypedHeader).addValue(rowMap.get(typedHeader));
            }
        }
        return columns;
    }
}
