package de.hpi.bpt.io;

import de.hpi.bpt.datastructures.ColumnDefinition;
import de.hpi.bpt.datastructures.Schema;
import org.supercsv.cellprocessor.*;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvMapReader;
import org.supercsv.prefs.CsvPreference;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Arrays;
import java.util.Date;

public class CsvLogReader {

    private String dateFormat = "yyyy-MM-dd'T'HH:mm:ssXXX";
    private char separator = ',';
    private String caseIdName = "caseid";
    private String timestampName = "timestamp";
    private String activityName = "activity";

    CsvMapReader read(File file) throws FileNotFoundException {
        var csvPreference = new CsvPreference.Builder('"', separator, "\r\n").build();
        return new CsvMapReader(new FileReader(file), csvPreference);
    }

    CellProcessor[] getProcessors(Schema schema) {
        var cellProcessors = new CellProcessor[schema.size()];
        var values = schema.values().toArray(new ColumnDefinition<?>[0]);
        var numValues = schema.keySet().size();
        for (int i = 0; i < numValues; i++) {
            if (cellProcessors[i] == null) {
                cellProcessors[i] = typeProcessorFor(values[i].getType());
            }
        }
        return cellProcessors;
    }


    Schema readSchemaFromHeader(String[] header) {
        var schema = new Schema();
        for (var headerField : header) {
            var headerDeclaration = headerField.split(":");
            var fieldName = headerDeclaration[0];
            var fieldTypeString = headerDeclaration[1];
            schema.addColumnDefinition(fieldName, typeFor(fieldTypeString));
        }
        schema.setCaseIdName(caseIdName);
        schema.setActivityName(activityName);
        schema.setTimestampName(timestampName);
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

    private CellProcessor typeProcessorFor(Class<?> value) {
        CellProcessor typeProcessor;
        if (Integer.class.equals(value)) {
            typeProcessor = new Optional(new ParseInt());
        } else if (Double.class.equals(value)) {
            typeProcessor = new Optional(new ParseDouble());
        } else if (Date.class.equals(value)) {
            typeProcessor = new Optional(new ParseDate(dateFormat));
        } else if (Boolean.class.equals(value)) {
            typeProcessor = new Optional(new ParseBool());
        } else {
            typeProcessor = new Optional();
        }
        return typeProcessor;
    }


    public CsvLogReader dateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
        return this;
    }

    public CsvLogReader separator(char separator) {
        this.separator = separator;
        return this;
    }

    public CsvLogReader caseIdName(String caseIdName) {
        this.caseIdName = caseIdName;
        return this;
    }

    public CsvLogReader timestampName(String timestampName) {
        this.timestampName = timestampName;
        return this;
    }

    public CsvLogReader activityName(String activityName) {
        this.activityName = activityName;
        return this;
    }

    public String getDateFormat() {
        return dateFormat;
    }

    public char getSeparator() {
        return separator;
    }
}
