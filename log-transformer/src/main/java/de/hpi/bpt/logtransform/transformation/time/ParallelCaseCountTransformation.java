package de.hpi.bpt.logtransform.transformation.time;

import de.hpi.bpt.logtransform.datastructures.CaseColumn;
import de.hpi.bpt.logtransform.datastructures.ColumnCaseLog;
import de.hpi.bpt.logtransform.datastructures.ColumnEventLog;
import de.hpi.bpt.logtransform.transformation.LogTransformation;

import java.util.Date;
import java.util.HashMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class ParallelCaseCountTransformation implements LogTransformation {

    @Override
    public void transform(ColumnEventLog sourceEventLog, ColumnCaseLog resultCaseLog) {
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
                    return Stream.of(new TimedCaseStartEvent(caseId, start), new TimedCaseEndEvent(caseId, end));
                })
                .flatMap(Function.identity())
                .sorted((e1, e2) -> e1.getDate().equals(e2.getDate()) ? Boolean.compare(e1.isEnd(), e2.isEnd()) : e1.getDate().compareTo(e2.getDate()))
                .collect(Collectors.toList());

        var currentCases = new HashMap<String, ConcurrentCaseCounter>();
        for (TimedCaseEvent timedCaseEvent : timedCases) {
            if (timedCaseEvent.isEnd()) {
                var numCurrentCases = currentCases.remove(timedCaseEvent.getCaseId()).getCount();
                parallelCasesColumn.setValue(resultCaseLog.rowIndexOf(timedCaseEvent.getCaseId()), numCurrentCases);
            } else { // is start
                currentCases.values().forEach(ConcurrentCaseCounter::increment);
                currentCases.put(timedCaseEvent.getCaseId(), new ConcurrentCaseCounter(currentCases.size()));
            }
        }

        resultCaseLog.put("numparallelcases", parallelCasesColumn);
    }

    private abstract static class TimedCaseEvent {
        private String caseId;
        private Date date;

        TimedCaseEvent(String caseId, Date date) {
            this.caseId = caseId;
            this.date = date;
        }

        String getCaseId() {
            return caseId;
        }

        Date getDate() {
            return date;
        }

        abstract boolean isEnd();
    }

    private static class TimedCaseStartEvent extends TimedCaseEvent {

        TimedCaseStartEvent(String caseId, Date date) {
            super(caseId, date);
        }

        @Override
        boolean isEnd() {
            return false;
        }
    }

    private static class TimedCaseEndEvent extends TimedCaseEvent {

        TimedCaseEndEvent(String caseId, Date date) {
            super(caseId, date);
        }

        @Override
        boolean isEnd() {
            return true;
        }
    }

    private static class ConcurrentCaseCounter {

        int concurrentCases;

        ConcurrentCaseCounter(int initialCount) {
            this.concurrentCases = initialCount;
        }

        void increment() {
            concurrentCases++;
        }

        int getCount() {
            return concurrentCases;
        }
    }
}
