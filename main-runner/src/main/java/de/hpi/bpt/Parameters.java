package de.hpi.bpt;

import java.util.List;

class Parameters {

    static final String FOLDER = "/home/jonas/Data/Macif/incidents/";

    static final String MODEL_FILE = "model.bpmn";
    static final String EVENTS_FILE = "events_sorted_no_night.csv";
    static final List<String> ATTRIBUTES_FILES = List.of("attributes1_sorted.csv");
    static final String GRAPH_OUTPUT_FILE = "tree.gv";

    static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
    static final char SEPARATOR = ',';

    static final String CASE_ID_NAME = "CaseID";
    static final String TIMESTAMP_NAME = "Timestamp";
    static final String ACTIVITY_NAME = "EventName";

    static final String TARGET_VARIABLE = "caseend_before_SLA";
}
