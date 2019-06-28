package de.hpi.bpt.transformation;

import de.hpi.bpt.datastructures.CaseColumn;
import de.hpi.bpt.datastructures.CaseLog;
import de.hpi.bpt.datastructures.EventLog;
import de.hpi.bpt.datastructures.Schema;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;

public class LogTransformer {

    private EventLog sourceEventLog;
    private Set<LogTransformation> transformations = new HashSet<>();

    public LogTransformer(EventLog sourceEventLog) {
        this.sourceEventLog = sourceEventLog;
    }

    public LogTransformer with(LogTransformation transformation) {
        transformations.add(transformation);
        return this;
    }

    public CaseLog transform() {
        var targetSchema = new Schema();
        var transformedColumns = new LinkedHashMap<String, CaseColumn<?>>();

        for (var transformation : transformations) {
            transformation.transform(sourceEventLog, targetSchema, transformedColumns);
        }

        return new CaseLog(targetSchema, transformedColumns);
    }
}
