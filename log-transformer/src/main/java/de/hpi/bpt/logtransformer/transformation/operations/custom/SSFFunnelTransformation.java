package de.hpi.bpt.logtransformer.transformation.operations.custom;

import de.hpi.bpt.logtransformer.transformation.datastructures.ColumnCaseLog;
import de.hpi.bpt.logtransformer.transformation.datastructures.ColumnEventLog;
import de.hpi.bpt.logtransformer.transformation.operations.LogTransformation;

import java.util.regex.Pattern;

public class SSFFunnelTransformation implements LogTransformation {

    private static final String STAGE_REGEX = "Opportunity stage set to ([0-9]).*";
    private static final Pattern STAGE_PATTERN = Pattern.compile(STAGE_REGEX);

    @Override
    public void transform(ColumnEventLog sourceEventLog, ColumnCaseLog resultCaseLog) {
        var activityColumn = sourceEventLog.getActivityColumn();

        var targetColumn = resultCaseLog.addColumn("Drop Out Stage", String.class);

        for (var trace : activityColumn.getTraces()) {
            var maxStage = trace.stream()
                    .filter(activity -> Pattern.matches(STAGE_REGEX, activity))
                    .mapToInt(activity -> {
                        var matcher = STAGE_PATTERN.matcher(activity);
                        matcher.find();
                        return Integer.parseInt(matcher.group(1));
                    })
                    .max();

            maxStage.ifPresentOrElse(
                    i -> targetColumn.addValue(String.valueOf(i)),
                    () -> targetColumn.addValue("NONE")
            );
        }
    }
}
