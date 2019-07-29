package de.hpi.bpt.transformation.posthoc;

import de.hpi.bpt.datastructures.RowCaseLog;
import de.hpi.bpt.datastructures.Schema;

import java.util.Date;

public class DateBeforeTransformation {

    private String firstAttributeName;
    private String secondAttributeName;

    public DateBeforeTransformation(String firstAttributeName, String secondAttributeName) {
        this.firstAttributeName = firstAttributeName;
        this.secondAttributeName = secondAttributeName;
    }

    public void transform(RowCaseLog caseLog) {
        var schema = caseLog.getSchema();
        if (!Date.class.equals(schema.get(firstAttributeName).getType())
                || !Date.class.equals(schema.get(secondAttributeName).getType())) {
            throw new RuntimeException(String.format("One of [%s, %s] is not of type date", firstAttributeName, secondAttributeName));
        }

        var firstAttributeIndex = schema.get(firstAttributeName).getId();
        var secondAttributeIndex = schema.get(secondAttributeName).getId();

        caseLog.forEach((caseId, row) -> {
            var firstDate = (Date) row.get(firstAttributeIndex);
            var secondDate = (Date) row.get(secondAttributeIndex);

            row.remove(firstDate);
            row.remove(secondDate);

            if (firstDate == null || secondDate == null) {
                row.add(null);
            } else {
                row.add(firstDate.before(secondDate));
            }

        });

        var newSchema = new Schema();
        schema.forEach((name, columnDefinition) -> {
            if (!name.equals(firstAttributeName) && !name.equals(secondAttributeName)) {
                newSchema.addColumnDefinition(name, columnDefinition.getType());
            }
        });
        newSchema.addColumnDefinition(String.format("%s_before_%s", firstAttributeName, secondAttributeName), Boolean.class);
        caseLog.setSchema(newSchema);
    }
}
