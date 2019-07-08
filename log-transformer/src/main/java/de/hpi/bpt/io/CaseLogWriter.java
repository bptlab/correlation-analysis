package de.hpi.bpt.io;

import de.hpi.bpt.datastructures.ColumnCaseLog;
import de.hpi.bpt.datastructures.RowCaseLog;
import de.hpi.bpt.logmanipulation.CaseLogConverter;

import java.io.FileWriter;
import java.io.IOException;

public interface CaseLogWriter {

    String writeToString(RowCaseLog caseLog);

    default String writeToString(ColumnCaseLog caseLog) {
        return writeToString(new CaseLogConverter().asRowCaseLog(caseLog));
    }

    default void writeToFile(RowCaseLog caseLog, String filePath) {
        try (var fileWriter = new FileWriter(filePath)) {
            fileWriter.write(writeToString(caseLog));
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    default void writeToFile(ColumnCaseLog caseLog, String filePath) {
        writeToFile(new CaseLogConverter().asRowCaseLog(caseLog), filePath);
    }
}
