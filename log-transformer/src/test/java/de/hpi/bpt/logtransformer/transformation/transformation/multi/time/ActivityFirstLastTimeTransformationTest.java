package de.hpi.bpt.logtransformer.transformation.transformation.multi.time;

import de.hpi.bpt.logtransformer.transformation.EventLogBuilder;
import de.hpi.bpt.logtransformer.transformation.transformation.LogTransformer;
import org.junit.jupiter.api.Test;

import java.text.SimpleDateFormat;

import static org.assertj.core.api.Assertions.assertThat;

class ActivityFirstLastTimeTransformationTest {

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
                .row(formatter.parse("01-01-2003 11"), "A1")
                .build()
                .build();

        var transformation = new ActivityFirstLastTimeTransformation();

        // Act
        var afterTransformation = new LogTransformer(sourceEventLog).with(transformation).transform();

        // Assert
        assertThat(afterTransformation.getSchema()).containsOnlyKeys(
                "caseId",
                "A1 (first occurrence - hour)", "A1 (first occurrence - day of week)", "A1 (first occurrence - day of month)", "A1 (first occurrence - day of year)", "A1 (first occurrence - month)", "A1 (first occurrence - year)",
                "A1 (last occurrence - hour)", "A1 (last occurrence - day of week)", "A1 (last occurrence - day of month)", "A1 (last occurrence - day of year)", "A1 (last occurrence - month)", "A1 (last occurrence - year)",
                "A2 (first occurrence - hour)", "A2 (first occurrence - day of week)", "A2 (first occurrence - day of month)", "A2 (first occurrence - day of year)", "A2 (first occurrence - month)", "A2 (first occurrence - year)",
                "A2 (last occurrence - hour)", "A2 (last occurrence - day of week)", "A2 (last occurrence - day of month)", "A2 (last occurrence - day of year)", "A2 (last occurrence - month)", "A2 (last occurrence - year)",
                "A3 (first occurrence - hour)", "A3 (first occurrence - day of week)", "A3 (first occurrence - day of month)", "A3 (first occurrence - day of year)", "A3 (first occurrence - month)", "A3 (first occurrence - year)",
                "A3 (last occurrence - hour)", "A3 (last occurrence - day of week)", "A3 (last occurrence - day of month)", "A3 (last occurrence - day of year)", "A3 (last occurrence - month)", "A3 (last occurrence - year)"
        );

        assertThat(afterTransformation.get("1")).containsExactly("1",
                9, "SATURDAY", 1, 1, "JANUARY", 2000,
                11, "WEDNESDAY", 1, 1, "JANUARY", 2003,
                18, "SUNDAY", 2, 2, "JANUARY", 2000,
                18, "SUNDAY", 2, 2, "JANUARY", 2000,
                12, "FRIDAY", 20, 354, "DECEMBER", 2002,
                12, "FRIDAY", 20, 354, "DECEMBER", 2002
        );

    }
}