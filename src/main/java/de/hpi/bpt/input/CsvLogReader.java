package de.hpi.bpt.input;

import de.hpi.bpt.datastructures.ColumnDefinition;
import de.hpi.bpt.datastructures.EventLog;
import de.hpi.bpt.datastructures.LogColumn;
import de.hpi.bpt.datastructures.Schema;
import org.supercsv.cellprocessor.*;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvMapReader;
import org.supercsv.io.ICsvMapReader;
import org.supercsv.prefs.CsvPreference;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

public class CsvLogReader {

    public static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssXXX";

    public EventLog read(File file) {

        try (var mapReader = new CsvMapReader(new FileReader(file), CsvPreference.STANDARD_PREFERENCE)) {
            var header = mapReader.getHeader(false);
            var schema = readSchemaFromHeader(header);
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

        var processors = getProcessors(schema);

        var currentCaseId = -1;
        Map<String, Object> rowMap;
        while ((rowMap = reader.read(header, processors)) != null) {
            var newCaseId = (int) rowMap.get(header[0]);

            if (newCaseId != currentCaseId) {
                columns.values().forEach(LogColumn::addNewTrace);
                currentCaseId = newCaseId;
            }

            var columnArray = columns.keySet().toArray(new String[0]);
            for (int i = 0; i < header.length; i++) {
                var typedHeader = header[i];
                var untypedHeader = columnArray[i];
                columns.get(untypedHeader).addValue(rowMap.get(typedHeader));
            }
        }
        return columns;
    }

    private CellProcessor[] getProcessors(Schema schema) {
        var cellProcessors = new CellProcessor[schema.size()];
        cellProcessors[0] = new NotNull(new ParseInt()); // caseid
        cellProcessors[1] = new NotNull(new ParseDate(DATE_FORMAT)); // timestamp
        cellProcessors[2] = new NotNull(); // activity
        var keys = schema.keySet().toArray(new String[0]);
        var values = schema.values().toArray(new ColumnDefinition<?>[0]);
        for (int i = 3; i < schema.keySet().size(); i++) {
            var typeProcessor = typeProcessorFor(values[i].getType());
            cellProcessors[i] = typeProcessor.map(Optional::new).orElseGet(Optional::new);
        }
        return cellProcessors;
    }


    private Schema readSchemaFromHeader(String[] header) {
        var schema = new Schema();
        for (var headerField : header) {
            var headerDeclaration = headerField.split(":");
            schema.addColumnDefinition(headerDeclaration[0], typeFor(headerDeclaration[1]));
        }
        return schema;
    }

    private Class<?> typeFor(String s) {
        if ("int".equals(s)) {
            return Integer.class;
        } else if ("string".equals(s)) {
            return String.class;
        } else if ("date".equals(s)) {
            return Date.class;
        } else if ("boolean".equals(s)) {
            return Boolean.class;
        } else if ("double".equals(s)) {
            return Double.class;
        } else {
            throw new RuntimeException("Invalid type found in header: '" + s + "'!");
        }
    }

    private java.util.Optional<CellProcessor> typeProcessorFor(Class<?> value) {
        CellProcessor typeProcessor;
        if (Integer.class.equals(value)) {
            typeProcessor = new ParseInt();
        } else if (Double.class.equals(value)) {
            typeProcessor = new ParseDouble();
        } else if (Date.class.equals(value)) {
            typeProcessor = new ParseDate(DATE_FORMAT);
        } else if (Boolean.class.equals(value)) {
            typeProcessor = new ParseBool();
        } else {
            return java.util.Optional.empty();
        }
        return java.util.Optional.of(typeProcessor);
    }
}
