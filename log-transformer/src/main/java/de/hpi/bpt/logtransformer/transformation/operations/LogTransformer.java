package de.hpi.bpt.logtransformer.transformation.operations;

import de.hpi.bpt.logtransformer.transformation.datastructures.ColumnCaseLog;
import de.hpi.bpt.logtransformer.transformation.datastructures.ColumnEventLog;
import de.hpi.bpt.logtransformer.transformation.datastructures.RowCaseLog;
import de.hpi.bpt.logtransformer.transformation.logmanipulation.CaseLogConverter;
import de.hpi.bpt.logtransformer.transformation.logmanipulation.RowCaseLogJoiner;

import java.util.*;

/**
 * Performs a number of {@link LogTransformation}s on an event log, producing a row-oriented case log.
 */
public class LogTransformer {

    private ColumnEventLog sourceEventLog;
    private Set<LogTransformation> transformations = new LinkedHashSet<>();

    public LogTransformer(ColumnEventLog sourceEventLog) {
        this.sourceEventLog = sourceEventLog;

        // Requirement for every other transformation, extracts and counts the unique cases
        transformations.add(new CaseIdTransformation());
    }

    public LogTransformer with(LogTransformation transformation) {
        transformations.add(transformation);
        return this;
    }

    public LogTransformer with(Collection<LogTransformation> transformations) {
        this.transformations.addAll(transformations);
        return this;
    }

    public RowCaseLog transform() {
        var resultCaseLog = new ColumnCaseLog();
        resultCaseLog.setName(sourceEventLog.getName() + "_case");

        for (var transformation : transformations) {
            System.out.println("\t\t- " + transformation.getClass().getSimpleName());
            transformation.transform(sourceEventLog, resultCaseLog);
        }

        return new CaseLogConverter().asRowCaseLog(resultCaseLog);
    }

    public RowCaseLog transformJoining(List<RowCaseLog> attributeLogs) {
        var joiner = new RowCaseLogJoiner();
        return attributeLogs.stream().reduce(transform(), joiner::join);
    }
}
