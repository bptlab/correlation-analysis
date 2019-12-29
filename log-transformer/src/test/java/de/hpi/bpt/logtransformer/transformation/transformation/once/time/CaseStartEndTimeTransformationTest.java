package de.hpi.bpt.logtransformer.transformation.transformation.once.time;

import de.hpi.bpt.logtransformer.transformation.EventLogBuilder;
import de.hpi.bpt.logtransformer.transformation.transformation.LogTransformer;
import de.hpi.bpt.logtransformer.transformation.transformation.once.time.CaseStartEndTimeTransformation;
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
                "Case start (hour)", "Case start (day of week)", "Case start (day of month)", "Case start (day of year)", "Case start (month)", "Case start (year)",
                "Case end (hour)", "Case end (day of week)", "Case end (day of month)", "Case end (day of year)", "Case end (month)", "Case end (year)"
        );

        assertThat(afterTransformation.get("1")).containsExactly("1",
                9, "SATURDAY", 1, 1, "JANUARY", 2000,
                12, "FRIDAY", 20, 354, "DECEMBER", 2002
        );
    }

}