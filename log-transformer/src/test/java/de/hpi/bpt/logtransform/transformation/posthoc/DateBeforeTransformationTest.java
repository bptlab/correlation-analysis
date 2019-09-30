package de.hpi.bpt.logtransform.transformation.posthoc;

import de.hpi.bpt.logtransform.datastructures.RowCaseLog;
import de.hpi.bpt.logtransform.datastructures.Schema;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

class DateBeforeTransformationTest {

    @Test
    void transform() {
        // Arrange
        var schema = new Schema();
        schema.addColumnDefinition("caseId", String.class);
        schema.addColumnDefinition("date1", Date.class);
        schema.addColumnDefinition("date2", Date.class);

        var rowCaseLog = new RowCaseLog("Test", schema);
        rowCaseLog.put("1", Stream.of("1", new Date(100), new Date(200)).collect(toList()));
        rowCaseLog.put("2", Stream.of("2", new Date(200), new Date(200)).collect(toList()));
        rowCaseLog.put("3", Stream.of("3", new Date(300), new Date(200)).collect(toList()));

        // Act
        new DateBeforeTransformation("date1", "date2").transform(rowCaseLog);

        // Assert
        assertThat(rowCaseLog.get("1").get(3)).isEqualTo(true);
        assertThat(rowCaseLog.get("2").get(3)).isEqualTo(false);
        assertThat(rowCaseLog.get("3").get(3)).isEqualTo(false);
    }
}