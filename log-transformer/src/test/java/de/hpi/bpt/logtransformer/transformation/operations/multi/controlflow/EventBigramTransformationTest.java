package de.hpi.bpt.logtransformer.transformation.operations.multi.controlflow;

import de.hpi.bpt.logtransformer.transformation.EventLogBuilder;
import de.hpi.bpt.logtransformer.transformation.operations.LogTransformer;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class EventBigramTransformationTest {

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

        var transformation = new EventBigramTransformation();

        // Act
        var afterTransformation = new LogTransformer(sourceEventLog).with(transformation).transform();

        // Assert
        assertThat(afterTransformation.getSchema()).containsOnlyKeys(
                "caseId",
                "#Transitions from 'A1' to 'A2'",
                "#Transitions from 'A1' to 'A3'",
                "#Transitions from 'A2' to 'A1'",
                "#Transitions from 'A2' to 'A3'",
                "#Transitions from 'A3' to 'A1'",
                "#Transitions from 'A3' to 'A2'"
        );

        assertThat(afterTransformation.get("1")).containsExactly("1", 1, null, 1, null, null, null);
        assertThat(afterTransformation.get("2")).containsExactly("2", 1, 1, 1, 0, 1, 0);
        assertThat(afterTransformation.get("3")).containsExactly("3", 3, null, 2, null, null, null);

    }

}