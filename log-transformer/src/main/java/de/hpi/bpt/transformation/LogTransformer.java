package de.hpi.bpt.transformation;

import de.hpi.bpt.datastructures.ColumnCaseLog;
import de.hpi.bpt.datastructures.ColumnEventLog;
import de.hpi.bpt.datastructures.RowCaseLog;
import de.hpi.bpt.feature.AnalysisResult;
import de.hpi.bpt.feature.AnalysisResultType;
import de.hpi.bpt.feature.FollowingActivityFeature;
import de.hpi.bpt.logmanipulation.CaseLogConverter;
import de.hpi.bpt.logmanipulation.RowCaseLogJoiner;
import de.hpi.bpt.transformation.controlflow.FollowingActivityTransformation;

import java.util.LinkedHashSet;
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
        analysisResults.forEach(analysisResult -> transformations.add(transformationFor(analysisResult)));
        return this;
    }

    private LogTransformation transformationFor(AnalysisResult analysisResult) {
        if (analysisResult.getType() == AnalysisResultType.FOLLOWING_ACTIVITY) {
            var feature = (FollowingActivityFeature) analysisResult;
            return new FollowingActivityTransformation(feature.getActivityNames());
        }

        throw new RuntimeException("Unknown type of AnalysisResult: '" + analysisResult.getType().name() + "'");
    }

    public RowCaseLog transform() {
        var resultCaseLog = new ColumnCaseLog();
        resultCaseLog.setName(sourceEventLog.getName() + "_case");

        for (var transformation : transformations) {
            transformation.transform(sourceEventLog, resultCaseLog);
        }

        return new CaseLogConverter().asRowCaseLog(resultCaseLog);
    }

    public RowCaseLog transformJoining(RowCaseLog otherAttributes) {
        return new RowCaseLogJoiner().join(transform(), otherAttributes);
    }
}
