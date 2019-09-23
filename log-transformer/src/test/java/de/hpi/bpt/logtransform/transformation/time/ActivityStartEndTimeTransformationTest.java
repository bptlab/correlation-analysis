package de.hpi.bpt.logtransform.transformation.time;

import de.hpi.bpt.logtransform.EventLogBuilder;
import de.hpi.bpt.logtransform.transformation.LogTransformer;
import org.junit.jupiter.api.Test;

import java.text.SimpleDateFormat;

import static org.assertj.core.api.Assertions.assertThat;

class ActivityStartEndTimeTransformationTest {

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

        var transformation = new ActivityStartEndTimeTransformation();

        // Act
        var afterTransformation = new LogTransformer(sourceEventLog).with(transformation).transform();

        // Assert
        assertThat(afterTransformation.getSchema()).containsOnlyKeys(
                "caseId",
                "A1_first", "A1_first_hour", "A1_first_day_of_week", "A1_first_day_of_month", "A1_first_day_of_year", "A1_first_month", "A1_first_year",
                "A1_last", "A1_last_hour", "A1_last_day_of_week", "A1_last_day_of_month", "A1_last_day_of_year", "A1_last_month", "A1_last_year",
                "A2_first", "A2_first_hour", "A2_first_day_of_week", "A2_first_day_of_month", "A2_first_day_of_year", "A2_first_month", "A2_first_year",
                "A2_last", "A2_last_hour", "A2_last_day_of_week", "A2_last_day_of_month", "A2_last_day_of_year", "A2_last_month", "A2_last_year",
                "A3_first", "A3_first_hour", "A3_first_day_of_week", "A3_first_day_of_month", "A3_first_day_of_year", "A3_first_month", "A3_first_year",
                "A3_last", "A3_last_hour", "A3_last_day_of_week", "A3_last_day_of_month", "A3_last_day_of_year", "A3_last_month", "A3_last_year"
        );

        assertThat(afterTransformation.get("1")).containsExactly("1",
                formatter.parse("01-01-2000 09"), 9, "SATURDAY", 1, 1, "JANUARY", 2000,
                formatter.parse("01-01-2003 11"), 11, "WEDNESDAY", 1, 1, "JANUARY", 2003,
                formatter.parse("02-01-2000 18"), 18, "SUNDAY", 2, 2, "JANUARY", 2000,
                formatter.parse("02-01-2000 18"), 18, "SUNDAY", 2, 2, "JANUARY", 2000,
                formatter.parse("20-12-2002 12"), 12, "FRIDAY", 20, 354, "DECEMBER", 2002,
                formatter.parse("20-12-2002 12"), 12, "FRIDAY", 20, 354, "DECEMBER", 2002
        );

    }
}