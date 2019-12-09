package de.hpi.bpt.logtransform.transformation.custom;

import de.hpi.bpt.logtransform.datastructures.ColumnCaseLog;
import de.hpi.bpt.logtransform.datastructures.ColumnEventLog;
import de.hpi.bpt.logtransform.transformation.LogTransformation;

import java.time.Duration;

public class BPIC2019TargetTransformation implements LogTransformation {

    @Override
    public void transform(ColumnEventLog sourceEventLog, ColumnCaseLog resultCaseLog) {
        var activityColumn = sourceEventLog.getActivityColumn();
        var timestampColumn = sourceEventLog.getTimestampColumn();
        var numReworkColumn = resultCaseLog.addColumn("Rework happened", Boolean.class);
        var poToGoodsColumn = resultCaseLog.addColumn("PO Item to Goods > 15d?", Boolean.class);
        var goodsToClearColumn = resultCaseLog.addColumn("Goods Receipt to Clear Invoice > 63d?", Boolean.class);


        var traces = activityColumn.getTraces();
        for (int i = 0; i < traces.size(); i++) {
            var trace = traces.get(i);

            var reworkActivity = trace.stream()
                    .map(String::toLowerCase)
                    .filter(activity -> activity.contains("change") || activity.contains("delete") || activity.contains("cancel") || activity.contains("reactivate"))
                    .findAny();
            numReworkColumn.addValue(reworkActivity.isPresent());

            var createPOItem = trace.lastIndexOf("Create Purchase Order Item");
            var recordGoodsReceipt = trace.lastIndexOf("Record Goods Receipt");
            var clearInvoice = trace.lastIndexOf("Clear Invoice");
            if (createPOItem == -1 || recordGoodsReceipt == -1) {
                poToGoodsColumn.addValue(null);
            } else {
                poToGoodsColumn.addValue(
                        Duration.between(
                                timestampColumn.get(i).get(createPOItem).toInstant(),
                                timestampColumn.get(i).get(recordGoodsReceipt).toInstant()
                        ).toDays() > 15);
            }

            if (recordGoodsReceipt == -1 || clearInvoice == -1) {
                goodsToClearColumn.addValue(null);
            } else {
                goodsToClearColumn.addValue(
                        (int) Duration.between(
                                timestampColumn.get(i).get(recordGoodsReceipt).toInstant(),
                                timestampColumn.get(i).get(clearInvoice).toInstant()
                        ).toDays() > 63);
            }
        }
    }
}
