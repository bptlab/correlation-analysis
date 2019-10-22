package de.hpi.bpt.logtransform.transformation.multi.time;

import de.hpi.bpt.logtransform.EventLogBuilder;
import de.hpi.bpt.logtransform.transformation.LogTransformer;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class StageTimeTransformationTest {

    private static final int TO_MINUTES = 1000 * 60;

    @Test
    void transform() {
        // Arrange
        var sourceEventLog = new EventLogBuilder()
                .schema()
                .build()
                .trace("1")
                .row(new Date(1L * TO_MINUTES), "A1")
                .row(new Date(2L * TO_MINUTES), "A2")
                .row(new Date(3L * TO_MINUTES), "A3")
                .row(new Date(4L * TO_MINUTES), "A4")
                .build()
                .trace("2")
                .row(new Date(1L * TO_MINUTES), "A1")
                .row(new Date(2L * TO_MINUTES), "A1")
                .row(new Date(3L * TO_MINUTES), "A1")
                .build()
                .trace("3")
                .row(new Date(1L * TO_MINUTES), "A1")
                .row(new Date(2L * TO_MINUTES), "A2")
                .row(new Date(3L * TO_MINUTES), "A1")
                .row(new Date(4L * TO_MINUTES), "A2")
                .build()
                .build();

        var activityToStage = Map.of("A1", "S1",
                "A2", "S2",
                "A3", "S3",
                "A4", "S3"
        );
        var transformation = new StageTimeTransformation(activityToStage);

        // Act
        var afterTransformation = new LogTransformer(sourceEventLog).with(transformation).transform();

        // Assert
        assertThat(afterTransformation.getSchema()).containsOnlyKeys("caseId",
                "Time spent in 'S1' (in minutes)", "Time from start until 'S1' (in minutes)", "Time from 'S1' until end (in minutes)",
                "Time spent in 'S2' (in minutes)", "Time from start until 'S2' (in minutes)", "Time from 'S2' until end (in minutes)",
                "Time spent in 'S3' (in minutes)", "Time from start until 'S3' (in minutes)", "Time from 'S3' until end (in minutes)"
        );

        var row1 = afterTransformation.get("1");
        var row2 = afterTransformation.get("2");
        var row3 = afterTransformation.get("3");

        assertThat(row1).containsExactly("1", 0, 0, 3, 1, 1, 2, 2, 2, 0);
        assertThat(row2).containsExactly("2", 2, 0, 0, 0, null, null, 0, null, null);
        assertThat(row3).containsExactly("3", 1, 0, 1, 2, 1, 0, 0, null, null);
    }

}