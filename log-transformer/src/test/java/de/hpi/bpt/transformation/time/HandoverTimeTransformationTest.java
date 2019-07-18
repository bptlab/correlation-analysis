package de.hpi.bpt.transformation.time;

import de.hpi.bpt.EventLogBuilder;
import de.hpi.bpt.transformation.LogTransformer;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class HandoverTimeTransformationTest {

    private static final int MILLISECONDS_FACTOR = 1000;

    @Test
    void transform() {
        // Arrange
        var sourceEventLog = new EventLogBuilder()
                .schema()
                .build()
                .trace("1")
                .row(new Date(0), "A1")
                .row(new Date(100 * MILLISECONDS_FACTOR), "A2")
                .build()
                .trace("2")
                .row(new Date(0), "A1")
                .row(new Date(100 * MILLISECONDS_FACTOR), "A2")
                .row(new Date(200 * MILLISECONDS_FACTOR), "A1")
                .row(new Date(400 * MILLISECONDS_FACTOR), "A2")
                .build()
                .trace("3")
                .row(new Date(0), "A1")
                .row(new Date(100 * MILLISECONDS_FACTOR), "A3")
                .row(new Date(200 * MILLISECONDS_FACTOR), "A2")
                .build()
                .build();

        var transformation = new HandoverTimeTransformation().with("A1", "A2");

        // Act
        var afterTransformation = new LogTransformer(sourceEventLog).with(transformation).transform();

        // Assert
        assertThat(afterTransformation.getSchema()).containsOnlyKeys("caseId", "A1_A2_handovertime");

        assertThat(afterTransformation.get("1")).containsExactly("1", 100);
        assertThat(afterTransformation.get("2")).containsExactly("2", 150);
        assertThat(afterTransformation.get("3")).containsExactly("3", 0);
    }

}