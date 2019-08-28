package de.hpi.bpt.logtransform.transformation;

import de.hpi.bpt.logtransform.EventLogBuilder;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class CaseIdTransformationTest {

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

        var transformation = new CaseIdTransformation();

        // Act
        var afterTransformation = new LogTransformer(sourceEventLog).with(transformation).transform();

        // Assert
        assertThat(afterTransformation.getSchema()).containsOnlyKeys("caseId");

        var row1 = afterTransformation.get("1");
        var row2 = afterTransformation.get("2");
        var row3 = afterTransformation.get("3");

        assertThat(row1).containsExactly("1");
        assertThat(row2).containsExactly("2");
        assertThat(row3).containsExactly("3");
    }
}