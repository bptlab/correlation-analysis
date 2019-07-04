package de.hpi.bpt.transformation.controlflow;

import de.hpi.bpt.EventLogBuilder;
import de.hpi.bpt.datastructures.CaseColumn;
import de.hpi.bpt.transformation.LogTransformer;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class ActivityExecutionTogetherTransformationTest {

    @Test
    void transform() {
        // Arrange
        var sourceEventLog = new EventLogBuilder()
                .schema()
                .build()
                .trace("1")
                .row(new Date(1L), "A1")
                .row(new Date(2L), "A2")
                .row(new Date(3L), "A3")
                .build()
                .trace("2")
                .row(new Date(4L), "A2")
                .row(new Date(5L), "A3")
                .build()
                .trace("3")
                .row(new Date(6L), "A3")
                .build()
                .build();

        var transformation = new ActivityExecutionTogetherTransformation().with("A2", "A3");

        // Act
        var afterTransformation = new LogTransformer(sourceEventLog).with(transformation).transform();

        // Assert
        assertThat(afterTransformation.getSchema()).containsOnlyKeys("caseId", "A2_A3_together");

        CaseColumn<Boolean> a2A3Together = afterTransformation.getTyped("A2_A3_together");

        assertThat(a2A3Together.getValues()).containsExactly(true, true, false);
    }

}