package de.hpi.bpt.logtransformer.transformation.operations.once.time;

import de.hpi.bpt.logtransformer.transformation.EventLogBuilder;
import de.hpi.bpt.logtransformer.transformation.operations.LogTransformer;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class CaseDurationTransformationTest {

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
                .build();

        var transformation = new CaseDurationTransformation();

        // Act
        var afterTransformation = new LogTransformer(sourceEventLog).with(transformation).transform();

        // Assert
        assertThat(afterTransformation.getSchema()).containsOnlyKeys("caseId", "Case duration (in minutes)");

        var row1 = afterTransformation.get("1");
        var row2 = afterTransformation.get("2");

        assertThat(row1).containsExactly("1", 200);
        assertThat(row2).containsExactly("2", 13);
    }

}