package de.hpi.bpt.logtransform.transformation;

import de.hpi.bpt.logtransform.datastructures.ColumnCaseLog;
import de.hpi.bpt.logtransform.datastructures.ColumnEventLog;
import de.hpi.bpt.logtransform.datastructures.RowCaseLog;
import de.hpi.bpt.logtransform.logmanipulation.CaseLogConverter;
import de.hpi.bpt.logtransform.logmanipulation.RowCaseLogJoiner;
import de.hpi.bpt.modelanalysis.feature.AnalysisResult;

import java.util.*;

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

    public LogTransformer withAnalysisResults(Set<AnalysisResult> analysisResults, Map<String, String> activityMapping) {
        var featureGenerator = new ModelFeatureGenerator(activityMapping);
        transformations.addAll(featureGenerator.from(analysisResults));
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

    public LogTransformer with(Collection<LogTransformation> transformations) {
        this.transformations.addAll(transformations);
        return this;
    }
}
