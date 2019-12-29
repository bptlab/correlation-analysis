package de.hpi.bpt.logtransformer.transformation.transformation.multi.time;

import de.hpi.bpt.logtransformer.transformation.EventLogBuilder;
import de.hpi.bpt.logtransformer.transformation.transformation.LogTransformer;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class ActivityTimeTransformationTest {

    private static final int TO_MINUTES = 1000 * 60;

    @Test
    void transform() {
        // Arrange
        var sourceEventLog = new EventLogBuilder()
                .schema()
                .build()
                .trace("1")
                .row(new Date(0), "A1")
                .row(new Date(100 * TO_MINUTES), "A2")
                .row(new Date(200 * TO_MINUTES), "A3")
                .build()
                .trace("2")
                .row(new Date(300 * TO_MINUTES), "A2")
                .row(new Date(313 * TO_MINUTES), "A3")
                .build()
                .trace("3")
                .row(new Date(400 * TO_MINUTES), "A1")
                .row(new Date(450 * TO_MINUTES), "A2")
                .row(new Date(500 * TO_MINUTES), "A1")
                .row(new Date(550 * TO_MINUTES), "A2")
                .build()
                .build();

        var transformation = new ActivityTimeTransformation();

        // Act
        var afterTransformation = new LogTransformer(sourceEventLog).with(transformation).transform();

        // Assert
        assertThat(afterTransformation.getSchema()).containsOnlyKeys("caseId",
                "A1 - Time spent (in minutes)", "A1 - Time from start (in minutes)", "A1 - Time until end (in minutes)",
                "A2 - Time spent (in minutes)", "A2 - Time from start (in minutes)", "A2 - Time until end (in minutes)",
                "A3 - Time spent (in minutes)", "A3 - Time from start (in minutes)", "A3 - Time until end (in minutes)"
        );

        var row1 = afterTransformation.get("1");
        var row2 = afterTransformation.get("2");
        var row3 = afterTransformation.get("3");

        assertThat(row1).containsExactly("1", 0, 0, 200, 100, 0, 100, 100, 100, 0);
        assertThat(row2).containsExactly("2", null, null, null, 0, 0, 13, 13, 0, 0);
        assertThat(row3).containsExactly("3", 50, 0, 50, 100, 0, 0, null, null, null);
    }

}