package de.hpi.bpt.io;

import de.hpi.bpt.datastructures.CaseColumn;
import de.hpi.bpt.datastructures.CaseLog;
import de.hpi.bpt.datastructures.Schema;
import org.supercsv.io.ICsvMapReader;

import java.io.File;
import java.io.IOException;
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

    public CaseLog read(File file) {
        try (var mapReader = basicReader.read(file)) {
            var header = mapReader.getHeader(false);
            var schema = basicReader.readSchemaFromHeader(header);
            var columns = readColumns(header, schema, mapReader);
            return new CaseLog(schema, columns);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
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
