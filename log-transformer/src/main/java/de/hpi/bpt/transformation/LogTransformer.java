package de.hpi.bpt.transformation;

import de.hpi.bpt.datastructures.CaseLog;
import de.hpi.bpt.datastructures.EventLog;

import java.util.LinkedHashSet;
import java.util.Set;

public class LogTransformer {

    private EventLog sourceEventLog;
    private Set<LogTransformation> transformations = new LinkedHashSet<>();

    public LogTransformer(EventLog sourceEventLog) {
        this.sourceEventLog = sourceEventLog;

        // Requirement for every other transformation, extracts and counts the unique cases
        transformations.add(new CaseIdTransformation());
    }

    public LogTransformer with(LogTransformation transformation) {
        transformations.add(transformation);
        return this;
    }

    public CaseLog transform() {
        var resultCaseLog = new CaseLog();

        for (var transformation : transformations) {
            transformation.transform(sourceEventLog, resultCaseLog);
        }

        return resultCaseLog;
    }
}
