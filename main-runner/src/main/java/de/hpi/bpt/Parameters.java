package de.hpi.bpt;

import java.util.List;

class Parameters {

    static final String FOLDER = "/home/jonas/Data/BPIC2019/";

    static final String MODEL_FILE = "model.bpmn";
    static final String EVENTS_FILE = "events_sorted.csv";
    static final List<String> ATTRIBUTES_FILES = List.of("caseattributes.csv");
    static final String GRAPH_OUTPUT_FILE = "tree.png";

    static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    static final char SEPARATOR = ',';

    static final String CASE_ID_NAME = "caseid";
    static final String TIMESTAMP_NAME = "timestamp";
    static final String ACTIVITY_NAME = "name";

    static final String TARGET_VARIABLE = "Record Goods Receipt_Clear Invoice_duration_below_threshold";
    static final String TARGET_VALUE = "true";
}
