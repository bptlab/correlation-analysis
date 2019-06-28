package de.hpi.bpt.transformation;

import de.hpi.bpt.datastructures.CaseColumn;
import de.hpi.bpt.datastructures.EventLog;
import de.hpi.bpt.datastructures.Schema;

import java.util.*;

public class ActivityAppearanceTransformation implements LogTransformation {

    private Set<String> activityNames = new HashSet<>();

    public ActivityAppearanceTransformation(Set<String> activityNames) {
        this.activityNames.addAll(activityNames);
    }

    public ActivityAppearanceTransformation(String... activityNames) {
        this.activityNames.addAll(Arrays.asList(activityNames));
    }

    @Override
    public void transform(EventLog sourceEventLog, Schema targetSchema, Map<String, CaseColumn<?>> transformedColumns) {
        var sourceSchema = sourceEventLog.getSchema();
        var activityColumn = sourceEventLog.getTyped(sourceSchema.getActivityName(), String.class);

        for (String activityName : activityNames) {
            targetSchema.addColumnDefinition(activityName + "_appearance", Boolean.class);
            var appearanceColumn = new CaseColumn<>(Boolean.class);

            for (List<String> trace : activityColumn.getTraces()) {
                if (trace.contains(activityName)) {
                    appearanceColumn.addValue(true);
                } else {
                    appearanceColumn.addValue(false);
                }
            }

            transformedColumns.put(activityName + "_appearance", appearanceColumn);
        }
    }
}
