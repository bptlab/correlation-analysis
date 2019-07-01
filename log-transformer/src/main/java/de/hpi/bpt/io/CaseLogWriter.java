package de.hpi.bpt.io;

import de.hpi.bpt.datastructures.CaseLog;

public interface CaseLogWriter {

    void writeToFile(CaseLog caseLog, String filePath);
}
