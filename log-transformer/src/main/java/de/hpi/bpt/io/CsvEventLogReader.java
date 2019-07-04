package de.hpi.bpt.io;

import de.hpi.bpt.datastructures.EventLog;
import de.hpi.bpt.datastructures.LogColumn;
import de.hpi.bpt.datastructures.Schema;
import org.supercsv.io.ICsvMapReader;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class CsvEventLogReader {

    private CsvLogReader basicReader;

    public CsvEventLogReader() {
        this.basicReader = new CsvLogReader();
    }

    public CsvEventLogReader(CsvLogReader csvLogReader) {
        this.basicReader = csvLogReader;
    }

    public EventLog read(File file) {
        try (var mapReader = basicReader.read(file)) {
            var header = mapReader.getHeader(false);
            var schema = basicReader.readSchemaFromHeader(header);
            var columns = readColumns(header, schema, mapReader);
            return new EventLog(schema, columns);
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