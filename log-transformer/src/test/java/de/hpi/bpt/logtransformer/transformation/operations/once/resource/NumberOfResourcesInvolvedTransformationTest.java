package de.hpi.bpt.logtransformer.transformation.operations.once.resource;

import de.hpi.bpt.logtransformer.transformation.EventLogBuilder;
import de.hpi.bpt.logtransformer.transformation.operations.LogTransformer;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class NumberOfResourcesInvolvedTransformationTest {

    @Test
    void transform() {
        // Arrange
        var sourceEventLog = new EventLogBuilder()
                .schema()
                .withResourceColumn()
                .build()
                .trace("1")
                .row(new Date(1L), "A1", "R1")
                .row(new Date(2L), "A2", "R1")
                .row(new Date(3L), "A3", "R1")
                .build()
                .trace("2")
                .row(new Date(4L), "A1", "R1")
                .row(new Date(5L), "A3", "R3")
                .row(new Date(6L), "A2", "R2")
                .row(new Date(7L), "A3", "R3")
                .build()
                .build();

        var transformation = new NumberOfResourcesInvolvedTransformation();

        // Act
        var afterTransformation = new LogTransformer(sourceEventLog).with(transformation).transform();

        // Assert
        assertThat(afterTransformation.getSchema()).containsOnlyKeys("caseId", "#Resources involved");

        var row1 = afterTransformation.get("1");
        var row2 = afterTransformation.get("2");

        assertThat(row1).containsExactly("1", 1);
        assertThat(row2).containsExactly("2", 3);
    }
}