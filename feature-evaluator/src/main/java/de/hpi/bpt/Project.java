package de.hpi.bpt;

import java.util.List;

public enum Project {

    SIGNAVIO_SALESFORCE_OPPS(
            "Signavio Salesforce Opportunities",
            "OppIsWon",
            "FALSE",
            List.of(" Team Member added: Pre- Sales_snumexecutions")
    ),

    BPIC2018(
            "BPIC2018",
            "late",
            "true",
            List.of()
    ),

    BPIC2019(
            "BPIC2019",
            "Vendor creates invoice_Clear Invoice_duration_below_threshold",
            "false",
            List.of()
    );


    private final String folderName;
    private final String targetVariable;
    private final String targetValue;
    private final List<String> assumedCorrelations;

    Project(String folderName, String targetVariable, String targetValue, List<String> assumedCorrelations) {
        this.folderName = folderName;
        this.targetVariable = targetVariable;
        this.targetValue = targetValue;
        this.assumedCorrelations = assumedCorrelations;
    }

    public String getFolderName() {
        return folderName;
    }

    public String getTargetVariable() {
        return targetVariable;
    }

    public String getTargetValue() {
        return targetValue;
    }

    public List<String> getAssumedCorrelations() {
        return assumedCorrelations;
    }
}
