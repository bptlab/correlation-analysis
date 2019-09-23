package de.hpi.bpt;

public enum Project {

    SIGNAVIO_SALESFORCE_OPPS(
            "Signavio Salesforce Opportunities",
            "OppIsWon",
            "FALSE"
    ),

    BPIC2018(
            "BPIC2018",
            "late",
            "true"
    ),

    BPIC2019(
            "BPIC2019",
            "Vendor creates invoice_Clear Invoice_duration_below_threshold",
            "false"
    );


    private final String folderName;
    private final String targetVariable;
    private final String targetValue;

    Project(String folderName, String targetVariable, String targetValue) {
        this.folderName = folderName;
        this.targetVariable = targetVariable;
        this.targetValue = targetValue;
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
}
