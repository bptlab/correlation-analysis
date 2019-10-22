package de.hpi.bpt.logtransform.transformation.multi.time;

import de.hpi.bpt.logtransform.EventLogBuilder;
import de.hpi.bpt.logtransform.transformation.LogTransformer;
import de.hpi.bpt.logtransform.transformation.once.time.ActivityBottleneckTransformation;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class ActivityBottleneckTransformationTest {

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
                .row(new Date(400 * MILLISECONDS_FACTOR), "A3")
                .row(new Date(600 * MILLISECONDS_FACTOR), "A4")
                .build()
                .trace("2")
                .row(new Date(300 * MILLISECONDS_FACTOR), "A2")
                .row(new Date(313 * MILLISECONDS_FACTOR), "A3")
                .build()
                .trace("3")
                .row(new Date(800 * MILLISECONDS_FACTOR), "A4")
                .build()
                .build();

        var transformation = new ActivityBottleneckTransformation();

        // Act
        var afterTransformation = new LogTransformer(sourceEventLog).with(transformation).transform();

        // Assert
        assertThat(afterTransformation.getSchema()).containsOnlyKeys("caseId", "Longest Executing Activity");

        var row1 = afterTransformation.get("1");
        var row2 = afterTransformation.get("2");
        var row3 = afterTransformation.get("3");

        assertThat(row1).containsExactly("1", "A2");
        assertThat(row2).containsExactly("2", "A2");
        assertThat(row3).containsExactly("3", "NONE");
    }

}