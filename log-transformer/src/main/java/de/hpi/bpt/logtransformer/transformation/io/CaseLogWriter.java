package de.hpi.bpt.logtransformer.transformation.io;

import de.hpi.bpt.logtransformer.transformation.datastructures.ColumnCaseLog;
import de.hpi.bpt.logtransformer.transformation.datastructures.RowCaseLog;
import de.hpi.bpt.logtransformer.transformation.logmanipulation.CaseLogConverter;

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
