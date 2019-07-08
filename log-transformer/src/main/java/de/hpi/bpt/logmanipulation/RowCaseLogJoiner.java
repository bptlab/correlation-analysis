package de.hpi.bpt.logmanipulation;

import de.hpi.bpt.datastructures.RowCaseLog;
import de.hpi.bpt.datastructures.Schema;

import java.util.ArrayList;

public class RowCaseLogJoiner {

    public RowCaseLog join(RowCaseLog caseLog1, RowCaseLog caseLog2) {
        var joined = new RowCaseLog(caseLog1.getName(), joinSchemas(caseLog1.getSchema(), caseLog2.getSchema()));

        var caseLog2CaseIdIndex = caseLog2.getSchema().get(caseLog2.getSchema().getCaseIdName()).getId();

        caseLog1.forEach((caseId, values) -> {
            var joinedValues = new ArrayList<>(values);
            var caseLog2Values = caseLog2.get(caseId);
            caseLog2Values.remove(caseLog2CaseIdIndex);
            joinedValues.addAll(caseLog2Values);
            joined.put(caseId, joinedValues);
        });

        return joined;
    }

    private Schema joinSchemas(Schema schema1, Schema schema2) {
        var result = new Schema();
        result.setCaseIdName(schema1.getCaseIdName());

        schema1.forEach((name, columnDefinition) -> result.addColumnDefinition(name, columnDefinition.getType()));
        schema2.forEach((name, columnDefinition) -> {
            if (!schema2.getCaseIdName().equals(name)) {
                result.addColumnDefinition(name, columnDefinition.getType());
            }
        });

        return result;
    }
}
