package de.hpi.bpt.logtransform.transformation.multi.resource;

import de.hpi.bpt.logtransform.EventLogBuilder;
import de.hpi.bpt.logtransform.transformation.LogTransformer;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class WasResourceInvolvedTransformationTest {

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
                .build()
                .trace("2")
                .row(new Date(8L), "A1", "R1")
                .row(new Date(9L), "A2", "R1")
                .row(new Date(10L), "A3", "R1")
                .build()
                .build();

        var transformation = new WasResourceInvolvedTransformation();

        // Act
        var afterTransformation = new LogTransformer(sourceEventLog).with(transformation).transform();

        // Assert
        assertThat(afterTransformation.getSchema()).containsOnlyKeys("caseId",
                "Resource 'R1' involved?", "Resource 'R2' involved?", "Resource 'R3' involved?"
        );

        var row1 = afterTransformation.get("1");
        var row2 = afterTransformation.get("2");

        assertThat(row1).containsExactly("1", true, true, true);
        assertThat(row2).containsExactly("2", true, false, false);
    }

}