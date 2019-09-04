package de.hpi.bpt.logtransform.io;

import de.hpi.bpt.logtransform.datastructures.RowCaseLog;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.stream.Collectors;

public class ArffCaseLogWriter implements CaseLogWriter {

    private String dateFormat = "yyyy-MM-dd'T'HH:mm:ssXXX";
    private SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);

    @Override
    public String writeToString(RowCaseLog caseLog) {
        StringBuilder sb = new StringBuilder();

        sb.append("@RELATION ").append(caseLog.getName()).append("\n\n");

        caseLog.getSchema().forEach((name, columnDefinition) ->
                sb.append("@ATTRIBUTE \"").append(name).append("\" ").append(typeFor(columnDefinition.getType())).append("\n"));

        sb.append("\n@DATA\n");
        var rows = caseLog.values();
        rows.forEach(row -> sb.append(row.stream().map(this::formatted).collect(Collectors.joining(","))).append("\n"));

        return sb.toString();
    }

    private String typeFor(Class<?> type) {
        if (Integer.class.equals(type) || Double.class.equals(type)) {
            return "NUMERIC";
        } else if (String.class.equals(type)) {
            return "STRING";
        } else if (Date.class.equals(type)) {
            return String.format("DATE \"%s\"", dateFormat);
        } else if (Boolean.class.equals(type)) {
            return String.format("{%s,%s}", Boolean.TRUE.toString(), Boolean.FALSE.toString());
        } else {
            throw new RuntimeException("Unexpected type during Arff writing: " + type.getSimpleName());
        }
    }

    private String formatted(Object o) {
        if (o == null) {
            return "?";
        } else if (o instanceof String) {
            return String.format("\"%s\"", o);
        } else if (o instanceof Date) {
            return String.format("\"%s\"", sdf.format((Date) o));
        } else {
            return o.toString();
        }
    }
}