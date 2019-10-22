package de.hpi.bpt.logtransform.transformation.multi.time;

import de.hpi.bpt.logtransform.EventLogBuilder;
import de.hpi.bpt.logtransform.transformation.LogTransformer;
import de.hpi.bpt.logtransform.transformation.once.time.CaseStartEndTimeTransformation;
import org.junit.jupiter.api.Test;

import java.text.SimpleDateFormat;

import static org.assertj.core.api.Assertions.assertThat;

class CaseStartEndTimeTransformationTest {

    @Test
    void transform() throws Exception {
        // Arrange
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH");
        var sourceEventLog = new EventLogBuilder()
                .schema()
                .build()
                .trace("1")
                .row(formatter.parse("01-01-2000 09"), "A1")
                .row(formatter.parse("02-01-2000 18"), "A2")
                .row(formatter.parse("20-12-2002 12"), "A3")
                .build()
                .build();

        var transformation = new CaseStartEndTimeTransformation();

        // Act
        var afterTransformation = new LogTransformer(sourceEventLog).with(transformation).transform();

        // Assert
        assertThat(afterTransformation.getSchema()).containsOnlyKeys(
                "caseId",
                "casestart", "Case start (hour)", "Case start (day of week", "casestart_day of month", "casestart_day of year", "casestart_month", "casestart_year)",
                "caseend", "Case end (hour", "caseend_day of week", "caseend_day of month", "caseend_day of year", "caseend_month", "caseend_year)"
        );

        assertThat(afterTransformation.get("1")).containsExactly("1",
                formatter.parse("01-01-2000 09"), 9, "SATURDAY", 1, 1, "JANUARY", 2000,
                formatter.parse("20-12-2002 12"), 12, "FRIDAY", 20, 354, "DECEMBER", 2002
        );
    }

}