package de.hpi.bpt.logtransform.transformation.multi.time;

import de.hpi.bpt.logtransform.EventLogBuilder;
import de.hpi.bpt.logtransform.transformation.LogTransformer;
import org.junit.jupiter.api.Test;

import java.text.SimpleDateFormat;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class StageStartEndTimeTransformationTest {

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

        var activityToStage = Map.of("A1", "S1",
                "A2", "S2",
                "A3", "S2"
        );
        var transformation = new StageStartEndTimeTransformation(activityToStage);

        // Act
        var afterTransformation = new LogTransformer(sourceEventLog).with(transformation).transform();

        // Assert
        assertThat(afterTransformation.getSchema()).containsOnlyKeys(
                "caseId",
                "S1 (first occurrence - hour)", "S1 (first occurrence - day of week)", "S1 (first occurrence - day of month)", "S1 (first occurrence - day of year)", "S1 (first occurrence - month)", "S1 (first occurrence - year)",
                "S1 (last occurrence - hour)", "S1 (last occurrence - day of week)", "S1 (last occurrence - day of month)", "S1 (last occurrence - day of year)", "S1 (last occurrence - month)", "S1 (last occurrence - year)",
                "S2 (first occurrence - hour)", "S2 (first occurrence - day of week)", "S2 (first occurrence - day of month)", "S2 (first occurrence - day of year)", "S2 (first occurrence - month)", "S2 (first occurrence - year)",
                "S2 (last occurrence - hour)", "S2 (last occurrence - day of week)", "S2 (last occurrence - day of month)", "S2 (last occurrence - day of year)", "S2 (last occurrence - month)", "S2 (last occurrence - year)"
        );

        assertThat(afterTransformation.get("1")).containsExactly("1",
                9, "SATURDAY", 1, 1, "JANUARY", 2000,
                11, "WEDNESDAY", 1, 1, "JANUARY", 2003,
                18, "SUNDAY", 2, 2, "JANUARY", 2000,
                12, "FRIDAY", 20, 354, "DECEMBER", 2002
        );

    }

}