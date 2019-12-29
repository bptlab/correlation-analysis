package de.hpi.bpt.logtransformer.transformation.operations.multi.resource;

import de.hpi.bpt.logtransformer.transformation.EventLogBuilder;
import de.hpi.bpt.logtransformer.transformation.operations.LogTransformer;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class ResourceHandoversTransformationTest {

    @Test
    void transform() {
        // Arrange
        var sourceEventLog = new EventLogBuilder()
                .schema()
                .withResourceColumn()
                .build()
                .trace("1")
                .row(new Date(1L), "A1", "R1")
                .row(new Date(2L), "A2", "R2")
                .row(new Date(3L), "A3", "R3")
                .row(new Date(4L), "A1", "R1")
                .row(new Date(5L), "A2", "R2")
                .build()
                .trace("2")
                .row(new Date(8L), "A1", "R1")
                .row(new Date(9L), "A2", "R1")
                .row(new Date(10L), "A3", "R1")
                .build()
                .build();

        var transformation = new ResourceHandoversTransformation();

        // Act
        var afterTransformation = new LogTransformer(sourceEventLog).with(transformation).transform();

        // Assert
        assertThat(afterTransformation.getSchema()).containsOnlyKeys("caseId",
                "#Handovers from 'R1' to 'R2'", "#Handovers from 'R1' to 'R3'",
                "#Handovers from 'R2' to 'R1'", "#Handovers from 'R2' to 'R3'",
                "#Handovers from 'R3' to 'R1'", "#Handovers from 'R3' to 'R2'"
        );

        var row1 = afterTransformation.get("1");
        var row2 = afterTransformation.get("2");

        assertThat(row1).containsExactly("1", 2, 0, 0, 1, 1, 0);
        assertThat(row2).containsExactly("2", null, null, null, null, null, null);
    }

}