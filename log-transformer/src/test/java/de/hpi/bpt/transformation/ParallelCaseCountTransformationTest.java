package de.hpi.bpt.transformation;

import de.hpi.bpt.EventLogBuilder;
import de.hpi.bpt.datastructures.CaseColumn;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class ParallelCaseCountTransformationTest {

    @Test
    void transform() {
        // Arrange
        var sourceEventLog = new EventLogBuilder()
                .schema()
                .build()
                .trace("1")
                .row(new Date(100L), "A1")
                .row(new Date(200L), "A2")
                .build()
                .trace("before 1")
                .row(new Date(0L), "A1")
                .row(new Date(40L), "A2")
                .build()
                .trace("into 1")
                .row(new Date(80L), "A1")
                .row(new Date(120L), "A2")
                .build()
                .trace("inside 1")
                .row(new Date(140L), "A1")
                .row(new Date(160L), "A2")
                .build()
                .trace("out of 1")
                .row(new Date(180L), "A1")
                .row(new Date(220L), "A2")
                .build()
                .trace("after 1")
                .row(new Date(240L), "A1")
                .row(new Date(260L), "A2")
                .build()
                .build();

        var transformation = new ParallelCaseCountTransformation();

        // Act
        var afterTransformation = new LogTransformer(sourceEventLog).with(transformation).transform();

        // Assert
        CaseColumn<Integer> caseId = afterTransformation.getTyped("numparallelcases");

        assertThat(caseId.getValues()).containsExactly(3, 0, 1, 1, 1, 0);
    }

}