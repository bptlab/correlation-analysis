package de.hpi.bpt.logtransformer.transformation.logmanipulation;

import de.hpi.bpt.logtransformer.transformation.datastructures.RowCaseLog;
import de.hpi.bpt.logtransformer.transformation.datastructures.Schema;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Joins row-oriented case logs by case id.
 */
public class RowCaseLogJoiner {

    public RowCaseLog join(RowCaseLog caseLog1, RowCaseLog caseLog2) {
        var joined = new RowCaseLog(caseLog1.getName(), joinSchemas(caseLog1.getSchema(), caseLog2.getSchema()));

        var caseLog2CaseIdIndex = caseLog2.getSchema().get(caseLog2.getSchema().getCaseIdName()).getId();

        var numCaseLog2Attributes = caseLog2.getSchema().size() - 1; // not counting the CaseId

        caseLog1.forEach((caseId, values) -> {
            var joinedValues = new ArrayList<>(values);
            List<Object> caseLog2Values;
            if (caseLog2.containsKey(caseId)) {
                caseLog2Values = caseLog2.get(caseId);
                caseLog2Values.remove(caseLog2CaseIdIndex);

            } else {
                caseLog2Values = IntStream.range(0, numCaseLog2Attributes).mapToObj(i -> null).collect(Collectors.toList());
            }
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
