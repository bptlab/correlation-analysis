package de.hpi.bpt.io;

import de.hpi.bpt.datastructures.CaseLog;
import de.hpi.bpt.datastructures.ColumnDefinition;
import de.hpi.bpt.datastructures.Schema;
import org.supercsv.cellprocessor.FmtDate;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvListWriter;
import org.supercsv.prefs.CsvPreference;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

public class CsvLogWriter implements CaseLogWriter {

    private String dateFormat = "yyyy-MM-dd'T'HH:mm:ssXXX";

    @Override
    public void writeToFile(CaseLog caseLog, String filePath) {
        try (var listWriter = new CsvListWriter(new FileWriter(filePath), CsvPreference.STANDARD_PREFERENCE)) {
            var schema = caseLog.getSchema();
            var processors = getProcessors(schema);

            var typedHeader = schema.entrySet().stream()
                    .map(entry -> entry.getKey() + ":" + typeNameFor(entry.getValue().getType()))
                    .toArray(String[]::new);

            listWriter.writeHeader(typedHeader);

            var rows = new CaseLogFormatter().asRows(caseLog, false);
            for (var row : rows) {
                listWriter.write(row, processors);
            }
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private CellProcessor[] getProcessors(Schema schema) {
        var cellProcessors = new CellProcessor[schema.size()];
        var values = schema.values().toArray(new ColumnDefinition<?>[0]);
        var numValues = schema.keySet().size();
        for (int i = 0; i < numValues; i++) {
            cellProcessors[i] = typeProcessorFor(values[i].getType());
        }
        return cellProcessors;
    }

    private CellProcessor typeProcessorFor(Class<?> value) {
        if (Date.class.equals(value)) {
            return new Optional(new FmtDate(dateFormat));
        } else {
            return new Optional();
        }
    }

    private String typeNameFor(Class<?> type) {
        if (Integer.class.equals(type)) {
            return "int";
        } else if (String.class.equals(type)) {
            return "string";
        } else if (Date.class.equals(type)) {
            return "date";
        } else if (Boolean.class.equals(type)) {
            return "boolean";
        } else if (Double.class.equals(type)) {
            return "double";
        } else {
            throw new RuntimeException("Invalid type found in header: '" + type.getSimpleName() + "'!");
        }
    }

    public CsvLogWriter dateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
        return this;
    }
}
