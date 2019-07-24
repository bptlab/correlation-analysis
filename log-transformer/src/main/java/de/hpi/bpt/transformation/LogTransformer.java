package de.hpi.bpt.transformation;

import de.hpi.bpt.datastructures.ColumnCaseLog;
import de.hpi.bpt.datastructures.ColumnEventLog;
import de.hpi.bpt.datastructures.RowCaseLog;
import de.hpi.bpt.feature.AnalysisResult;
import de.hpi.bpt.logmanipulation.CaseLogConverter;
import de.hpi.bpt.logmanipulation.RowCaseLogJoiner;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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

    public LogTransformer withAnalysisResults(Set<AnalysisResult> analysisResults) {
        var featureGenerator = new FeatureGenerator();
        analysisResults.forEach(analysisResult -> transformations.add(featureGenerator.from(analysisResult)));
        return this;
    }

    public RowCaseLog transform() {
        var resultCaseLog = new ColumnCaseLog();
        resultCaseLog.setName(sourceEventLog.getName() + "_case");

        for (var transformation : transformations) {
            transformation.transform(sourceEventLog, resultCaseLog);
        }

        return new CaseLogConverter().asRowCaseLog(resultCaseLog);
    }

    public RowCaseLog transformJoining(List<RowCaseLog> attributeLogs) {
        var joiner = new RowCaseLogJoiner();
        return attributeLogs.stream().reduce(transform(), joiner::join);
    }
}
