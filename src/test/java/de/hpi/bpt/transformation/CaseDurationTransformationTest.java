package de.hpi.bpt.transformation;

import de.hpi.bpt.EventLogBuilder;
import de.hpi.bpt.datastructures.CaseColumn;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class CaseDurationTransformationTest {

    private static final int NANO_SECONDS_FACTOR = 1000;

    @Test
    void transform() {
        // Arrange
        var sourceEventLog = new EventLogBuilder()
                .schema()
                .build()
                .trace("1")
                .row(new Date(0), "A1")
                .row(new Date(100 * NANO_SECONDS_FACTOR), "A2")
                .row(new Date(200 * NANO_SECONDS_FACTOR), "A3")
                .build()
                .trace("2")
                .row(new Date(300 * NANO_SECONDS_FACTOR), "A2")
                .row(new Date(313 * NANO_SECONDS_FACTOR), "A3")
                .build()
                .build();

        var transformation = new CaseDurationTransformation();

        // Act
        var afterTransformation = new LogTransformer(sourceEventLog).with(transformation).transform();

        // Assert
        CaseColumn<Integer> durations = afterTransformation.getTyped("duration");

        assertThat(durations.getValues()).containsExactly(200, 13);
    }

}