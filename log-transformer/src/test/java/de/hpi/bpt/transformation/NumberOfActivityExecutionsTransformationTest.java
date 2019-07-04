package de.hpi.bpt.transformation;

import de.hpi.bpt.EventLogBuilder;
import de.hpi.bpt.datastructures.CaseColumn;
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

        CaseColumn<Integer> a1Appearance = afterTransformation.getTyped("A1_numexecutions");
        CaseColumn<Integer> a2Appearance = afterTransformation.getTyped("A2_numexecutions");
        CaseColumn<Integer> a3Appearance = afterTransformation.getTyped("A3_numexecutions");

        assertThat(a1Appearance.getValues()).containsExactly(3, 0, 0);
        assertThat(a2Appearance.getValues()).containsExactly(1, 3, 0);
        assertThat(a3Appearance.getValues()).containsExactly(0, 1, 3);
    }

}