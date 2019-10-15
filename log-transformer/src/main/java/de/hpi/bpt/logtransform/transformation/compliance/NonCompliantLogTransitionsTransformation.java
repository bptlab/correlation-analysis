package de.hpi.bpt.logtransform.transformation.compliance;

import de.hpi.bpt.logtransform.datastructures.ColumnCaseLog;
import de.hpi.bpt.logtransform.datastructures.ColumnEventLog;
import de.hpi.bpt.logtransform.transformation.LogTransformation;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NonCompliantLogTransitionsTransformation implements LogTransformation {

    private final Map<String, List<String>> compliantFlows = new HashMap<>();

    public NonCompliantLogTransitionsTransformation() {
    }

    public NonCompliantLogTransitionsTransformation(Map<String, List<String>> compliantFlows) {
        this.compliantFlows.putAll(compliantFlows);
    }

    public NonCompliantLogTransitionsTransformation with(String activity, String... compliantFollowers) {
        compliantFlows.put(activity, Arrays.asList(compliantFollowers));
        return this;
    }


    @Override
    public void transform(ColumnEventLog sourceEventLog, ColumnCaseLog resultCaseLog) {
        var activityColumn = sourceEventLog.getActivityColumn();
        var violationsColumn = resultCaseLog.addColumn("#Invalid Transitions", Integer.class);

        for (List<String> trace : activityColumn.getTraces()) {
            var numViolations = 0;
            for (int i = 0; i < trace.size() - 1; i++) {

                if (compliantFlows.containsKey(trace.get(i))
                        && !compliantFlows.get(trace.get(i)).contains(trace.get(i + 1))) {
                    numViolations++;
                }
            }
            var lastEvent = trace.get(trace.size() - 1);
            if (compliantFlows.containsKey(lastEvent) && !compliantFlows.get(lastEvent).contains("#END#")) {
                numViolations++;
            }
            violationsColumn.addValue(numViolations);
        }
    }
}
