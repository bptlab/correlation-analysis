package de.hpi.bpt.logtransformer.transformation.transformation.multi.time;

import de.hpi.bpt.logtransformer.transformation.EventLogBuilder;
import de.hpi.bpt.logtransformer.transformation.transformation.LogTransformer;
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
                "S1 - Time spent (in minutes)", "S1 - Time from start (in minutes)", "S1 - Time until end (in minutes)",
                "S2 - Time spent (in minutes)", "S2 - Time from start (in minutes)", "S2 - Time until end (in minutes)",
                "S3 - Time spent (in minutes)", "S3 - Time from start (in minutes)", "S3 - Time until end (in minutes)"
        );

        var row1 = afterTransformation.get("1");
        var row2 = afterTransformation.get("2");
        var row3 = afterTransformation.get("3");

        assertThat(row1).containsExactly("1", 0, 0, 3, 1, 0, 2, 2, 1, 0);
        assertThat(row2).containsExactly("2", 2, 0, 0, 0, null, null, 0, null, null);
        assertThat(row3).containsExactly("3", 1, 0, 1, 2, 0, 0, 0, null, null);
    }

}