package de.hpi.bpt.logtransform.transformation.multi.time;

import de.hpi.bpt.logtransform.EventLogBuilder;
import de.hpi.bpt.logtransform.transformation.LogTransformer;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class BetweenEventsDurationTransformationTest {

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
                .row(new Date(200 * MILLISECONDS_FACTOR), "A3")
                .build()
                .trace("2")
                .row(new Date(300 * MILLISECONDS_FACTOR), "A2")
                .row(new Date(305 * MILLISECONDS_FACTOR), "A1")
                .row(new Date(313 * MILLISECONDS_FACTOR), "A3")
                .build()
                .build();

        var transformation = new BetweenEventsDurationTransformation("A1", "A3");

        // Act
        var afterTransformation = new LogTransformer(sourceEventLog).with(transformation).transform();

        // Assert
        assertThat(afterTransformation.getSchema()).containsOnlyKeys("caseId", "A1_A3_duration_between");

        var row1 = afterTransformation.get("1");
        var row2 = afterTransformation.get("2");

        assertThat(row1).containsExactly("1", 200);
        assertThat(row2).containsExactly("2", 8);
    }

}