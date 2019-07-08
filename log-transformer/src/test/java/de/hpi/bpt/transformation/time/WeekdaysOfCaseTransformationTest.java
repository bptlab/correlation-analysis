package de.hpi.bpt.transformation.time;

import de.hpi.bpt.EventLogBuilder;
import de.hpi.bpt.transformation.LogTransformer;
import org.junit.jupiter.api.Test;

import java.text.SimpleDateFormat;

import static org.assertj.core.api.Assertions.assertThat;

class WeekdaysOfCaseTransformationTest {

    @Test
    void transform() throws Exception {

        var dateFormat = new SimpleDateFormat("dd/MM/yyyy");

        var monday = dateFormat.parse("01/07/2019");
        var tuesday = dateFormat.parse("02/07/2019");
        var wednesday = dateFormat.parse("03/07/2019");
        var thursday = dateFormat.parse("04/07/2019");
        var friday = dateFormat.parse("05/07/2019");
        var saturday = dateFormat.parse("06/07/2019");
        var sunday = dateFormat.parse("07/07/2019");

        // Arrange
        var sourceEventLog = new EventLogBuilder()
                .schema()
                .build()
                .trace("starts monday")
                .row(monday, "A1")
                .row(tuesday, "A2")
                .row(sunday, "A3")
                .build()
                .trace("starts wednesday")
                .row(wednesday, "A1")
                .row(saturday, "A2")
                .build()
                .trace("starts thursday")
                .row(thursday, "A1")
                .row(friday, "A2")
                .build()
                .build();

        var transformation = new WeekdaysOfCaseTransformation();

        // Act
        var afterTransformation = new LogTransformer(sourceEventLog).with(transformation).transform();

        // Assert
        assertThat(afterTransformation.getSchema()).containsOnlyKeys("caseId", "casestartweekday", "caseendweekday");

        var rowMonday = afterTransformation.get("starts monday");
        var rowWednesday = afterTransformation.get("starts wednesday");
        var rowThursday = afterTransformation.get("starts thursday");

        assertThat(rowMonday).containsExactly("starts monday", "Monday", "Sunday");
        assertThat(rowWednesday).containsExactly("starts wednesday", "Wednesday", "Saturday");
        assertThat(rowThursday).containsExactly("starts thursday", "Thursday", "Friday");
    }

}