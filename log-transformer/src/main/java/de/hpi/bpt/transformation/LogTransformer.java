package de.hpi.bpt.transformation;

import de.hpi.bpt.datastructures.ColumnCaseLog;
import de.hpi.bpt.datastructures.ColumnEventLog;
import de.hpi.bpt.datastructures.RowCaseLog;
import de.hpi.bpt.feature.AnalysisResult;
import de.hpi.bpt.feature.AnalysisResultType;
import de.hpi.bpt.feature.LaneSwitchFeature;
import de.hpi.bpt.feature.XorSplitFollowsFeature;
import de.hpi.bpt.logmanipulation.CaseLogConverter;
import de.hpi.bpt.logmanipulation.RowCaseLogJoiner;
import de.hpi.bpt.transformation.controlflow.ActivityExecutionIndirectFlowTransformation;
import de.hpi.bpt.transformation.time.HandoverTimeTransformation;
import org.apache.commons.lang3.tuple.Pair;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static de.hpi.bpt.transformation.ActivityMapping.ACTIVITY_MAPPING;

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
        if (analysisResult.getType() == AnalysisResultType.XOR_SPLIT_FOLLOWS) {
            var feature = (XorSplitFollowsFeature) analysisResult;
            var eventPairs = feature.getActivityPairs().stream()
                    .filter(pair -> ACTIVITY_MAPPING.containsKey(pair.getLeft()) && ACTIVITY_MAPPING.containsKey(pair.getRight()))
                    .map(pair -> Pair.of(ACTIVITY_MAPPING.get(pair.getLeft()), ACTIVITY_MAPPING.get(pair.getRight())))
                    .collect(Collectors.toSet());
            return new ActivityExecutionIndirectFlowTransformation(eventPairs);
        } else if (analysisResult.getType() == AnalysisResultType.LANE_SWITCH) {
            var feature = (LaneSwitchFeature) analysisResult;
            var eventPairs = feature.getActivityPairs().stream()
                    .filter(pair -> ACTIVITY_MAPPING.containsKey(pair.getLeft()) && ACTIVITY_MAPPING.containsKey(pair.getRight()))
                    .map(pair -> Pair.of(ACTIVITY_MAPPING.get(pair.getLeft()), ACTIVITY_MAPPING.get(pair.getRight())))
                    .collect(Collectors.toSet());
            return new HandoverTimeTransformation(eventPairs);
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

    public RowCaseLog transformJoining(List<RowCaseLog> attributeLogs) {
        var joiner = new RowCaseLogJoiner();
        return attributeLogs.stream().reduce(transform(), joiner::join);
    }
}
