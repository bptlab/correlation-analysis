package de.hpi.bpt.logtransform.io;

import de.hpi.bpt.logtransform.datastructures.ColumnEventLog;
import de.hpi.bpt.logtransform.datastructures.LogColumn;
import de.hpi.bpt.logtransform.datastructures.Schema;
import org.supercsv.io.ICsvMapReader;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class CsvEventLogReader {

    private CsvLogReader basicReader;
    private CsvEventLogValidator validator;

    public CsvEventLogReader(CsvLogReader csvLogReader) {
        this.basicReader = csvLogReader;
        this.validator = new CsvEventLogValidator();
    }

    public ColumnEventLog read(File file) {
        try (var mapReader = basicReader.read(file)) {
            var header = mapReader.getHeader(false);
            var schema = basicReader.readSchemaFromHeader(header);
            var columns = readColumns(header, schema, mapReader);
            var fileParts = file.getName().split("/");
            var logName = fileParts[fileParts.length - 1].replace(".csv", "");
            var columnEventLog = new ColumnEventLog(logName, schema, columns);

            if (!validator.isValid(columnEventLog)) {
                throw new RuntimeException("Event log needs to be sorted by caseId and timestamp!");
            }

            return columnEventLog;
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private Map<String, LogColumn<?>> readColumns(String[] header, Schema schema, ICsvMapReader reader) throws IOException {
        var columns = new LinkedHashMap<String, LogColumn<?>>();
        for (var entry : schema.entrySet()) {
            columns.put(entry.getKey(), new LogColumn<>(entry.getValue().getType()));
        }
        var columnArray = columns.keySet().toArray(new String[0]);

        var processors = basicReader.getProcessors(schema);

        String currentCaseId = "-";
        Map<String, Object> rowMap;
        var count = 0;
        while ((rowMap = reader.read(header, processors)) != null) {
            var newCaseId = (String) rowMap.get(header[0]);

            if (!currentCaseId.equals(newCaseId)) {
                columns.values().forEach(LogColumn::addNewTrace);
                currentCaseId = newCaseId;
            }

            for (int i = 0; i < header.length; i++) {
                var typedHeader = header[i];
                var untypedHeader = columnArray[i];
                columns.get(untypedHeader).addValue(rowMap.get(typedHeader));
            }
        }
        return columns;
    }

}
