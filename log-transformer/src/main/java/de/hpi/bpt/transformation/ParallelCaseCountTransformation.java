package de.hpi.bpt.transformation;

import de.hpi.bpt.datastructures.CaseColumn;
import de.hpi.bpt.datastructures.CaseLog;
import de.hpi.bpt.datastructures.EventLog;

import java.util.Comparator;
import java.util.Date;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ParallelCaseCountTransformation implements LogTransformation {

    @Override
    public void transform(EventLog sourceEventLog, CaseLog resultCaseLog) {
        var targetSchema = resultCaseLog.getSchema();
        targetSchema.addColumnDefinition("numparallelcases", Integer.class);
        var parallelCasesColumn = new CaseColumn<>(Integer.class, resultCaseLog.getNumCases());

        var caseTimestamps = sourceEventLog.getTyped(sourceEventLog.getSchema().getTimestampName(), Date.class);
        var caseIds = resultCaseLog.getCaseIds();

        var timedCases = IntStream.range(0, caseIds.size())
                .mapToObj(i -> {
                    var caseId = caseIds.get(i);
                    var currentCaseTimestamps = caseTimestamps.get(i);
                    var start = currentCaseTimestamps.get(0);
                    var end = currentCaseTimestamps.get(currentCaseTimestamps.size() - 1);
                    return new TimedCase(caseId, start, end);
                })
                .sorted(Comparator.comparing(TimedCase::getStart))
                .collect(Collectors.toList());

        for (TimedCase timedCase : timedCases) {
            var numOverlapping = 0;

            for (TimedCase otherCase : timedCases) {
                if (timedCase == otherCase) {
                    continue;
                }
                if (!otherCase.getStart().before(timedCase.getEnd())) {
                    break;
                } else if (otherCase.getEnd().after(timedCase.getStart())) {
                    numOverlapping++;
                }
            }

            parallelCasesColumn.setValue(resultCaseLog.rowIndexOf(timedCase.getCaseId()), numOverlapping);
        }

        resultCaseLog.put("numparallelcases", parallelCasesColumn);
    }

    private class TimedCase {
        private String caseId;
        private Date start;
        private Date end;

        TimedCase(String caseId, Date start, Date end) {
            this.caseId = caseId;
            this.start = start;
            this.end = end;
        }

        String getCaseId() {
            return caseId;
        }

        Date getStart() {
            return start;
        }

        Date getEnd() {
            return end;
        }
    }
}
