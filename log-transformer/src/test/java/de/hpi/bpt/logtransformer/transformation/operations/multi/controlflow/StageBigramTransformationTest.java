package de.hpi.bpt.logtransformer.transformation.operations.multi.controlflow;

import de.hpi.bpt.logtransformer.transformation.EventLogBuilder;
import de.hpi.bpt.logtransformer.transformation.operations.LogTransformer;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class StageBigramTransformationTest {

    @Test
    void transform() {
        // Arrange
        var sourceEventLog = new EventLogBuilder()
                .schema()
                .build()
                .trace("1")
                .row(new Date(1L), "A1")
                .row(new Date(2L), "A2")
                .row(new Date(3L), "A2")
                .row(new Date(4L), "Another")
                .row(new Date(5L), "A1")
                .build()
                .trace("2")
                .row(new Date(1L), "A1")
                .row(new Date(2L), "A2")
                .row(new Date(3L), "A1")
                .row(new Date(4L), "A3")
                .row(new Date(5L), "A1")
                .build()
                .trace("3")
                .row(new Date(1L), "A1")
                .row(new Date(2L), "A2")
                .row(new Date(3L), "A1")
                .row(new Date(4L), "A2")
                .row(new Date(5L), "A1")
                .row(new Date(6L), "A2")
                .build()
                .build();

        var activityToStage = Map.of("A1", "S1",
                "A2", "S2",
                "A3", "S3"
        );
        var transformation = new StageBigramTransformation(activityToStage);

        // Act
        var afterTransformation = new LogTransformer(sourceEventLog).with(transformation).transform();

        // Assert
        assertThat(afterTransformation.getSchema()).containsOnlyKeys(
                "caseId",
                "#Transitions from 'S1' to 'S2'",
                "#Transitions from 'S1' to 'S3'",
                "#Transitions from 'S2' to 'S1'",
                "#Transitions from 'S2' to 'S3'",
                "#Transitions from 'S3' to 'S1'",
                "#Transitions from 'S3' to 'S2'"
        );

        assertThat(afterTransformation.get("1")).containsExactly("1", 1, 0, 0, 0, 0, 0);
        assertThat(afterTransformation.get("2")).containsExactly("2", 1, 1, 1, 0, 1, 0);
        assertThat(afterTransformation.get("3")).containsExactly("3", 3, 0, 2, 0, 0, 0);

    }

}