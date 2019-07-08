package de.hpi.bpt.transformation.controlflow;

import de.hpi.bpt.EventLogBuilder;
import de.hpi.bpt.transformation.LogTransformer;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class NumberOfActivityExecutionsTransformationTest {

    @Test
    void transform() {
        // Arrange
        var sourceEventLog = new EventLogBuilder()
                .schema()
                .build()
                .trace("1")
                .row(new Date(1L), "A1")
                .row(new Date(2L), "A1")
                .row(new Date(3L), "A2")
                .row(new Date(4L), "A1")
                .build()
                .trace("2")
                .row(new Date(1L), "A2")
                .row(new Date(2L), "A2")
                .row(new Date(3L), "A3")
                .row(new Date(4L), "A2")
                .build()
                .trace("3")
                .row(new Date(1L), "A3")
                .row(new Date(2L), "A3")
                .row(new Date(3L), "A4")
                .row(new Date(4L), "A3")
                .build()
                .build();

        var transformation = new NumberOfActivityExecutionsTransformation("A1", "A2", "A3");

        // Act
        var afterTransformation = new LogTransformer(sourceEventLog).with(transformation).transform();

        // Assert
        assertThat(afterTransformation.getSchema()).containsOnlyKeys("caseId", "A1_numexecutions", "A2_numexecutions", "A3_numexecutions");

        var row1 = afterTransformation.get("1");
        var row2 = afterTransformation.get("2");
        var row3 = afterTransformation.get("3");

        assertThat(row1).containsExactly("1", 3, 1, 0);
        assertThat(row2).containsExactly("2", 0, 3, 1);
        assertThat(row3).containsExactly("3", 0, 0, 3);
    }

}