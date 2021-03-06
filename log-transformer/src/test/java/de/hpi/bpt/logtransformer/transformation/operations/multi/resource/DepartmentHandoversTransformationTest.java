package de.hpi.bpt.logtransformer.transformation.operations.multi.resource;

import de.hpi.bpt.logtransformer.transformation.EventLogBuilder;
import de.hpi.bpt.logtransformer.transformation.operations.LogTransformer;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class DepartmentHandoversTransformationTest {

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
                .row(new Date(4L), "A1")
                .row(new Date(5L), "A3")
                .row(new Date(6L), "A2")
                .row(new Date(7L), "A3")
                .build()
                .trace("3")
                .row(new Date(8L), "A1")
                .row(new Date(9L), "A2")
                .row(new Date(10L), "A3")
                .row(new Date(11L), "A2")
                .row(new Date(12L), "A3")
                .build()
                .build();

        var transformation = new DepartmentHandoversTransformation()
                .with("A1", "L1")
                .with("A2", "L1")
                .with("A3", "L2");

        // Act
        var afterTransformation = new LogTransformer(sourceEventLog).with(transformation).transform();

        // Assert
        assertThat(afterTransformation.getSchema()).containsOnlyKeys("caseId", "#Handovers from 'L1' to 'L2'", "#Handovers from 'L2' to 'L1'");

        var row1 = afterTransformation.get("1");
        var row2 = afterTransformation.get("2");
        var row3 = afterTransformation.get("3");

        assertThat(row1).containsExactly("1", 1, 0);
        assertThat(row2).containsExactly("2", 2, 1);
        assertThat(row3).containsExactly("3", 2, 1);
    }
}