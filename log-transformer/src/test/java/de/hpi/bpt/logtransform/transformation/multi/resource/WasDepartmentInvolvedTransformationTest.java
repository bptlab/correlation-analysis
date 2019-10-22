package de.hpi.bpt.logtransform.transformation.multi.resource;

import de.hpi.bpt.logtransform.EventLogBuilder;
import de.hpi.bpt.logtransform.transformation.LogTransformer;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class WasDepartmentInvolvedTransformationTest {
    @Test
    void transform() {
        // Arrange
        var sourceEventLog = new EventLogBuilder()
                .schema()
                .build()
                .trace("1")
                .row(new Date(1L), "A1")
                .row(new Date(2L), "A2")
                .build()
                .trace("2")
                .row(new Date(5L), "A3")
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

        var transformation = new WasDepartmentInvolvedTransformation()
                .with("A1", "L1")
                .with("A2", "L1")
                .with("A3", "L2");

        // Act
        var afterTransformation = new LogTransformer(sourceEventLog).with(transformation).transform();

        // Assert
        assertThat(afterTransformation.getSchema()).containsOnlyKeys("caseId", "Department 'L1' involved?", "Department 'L2' involved?");

        var row1 = afterTransformation.get("1");
        var row2 = afterTransformation.get("2");
        var row3 = afterTransformation.get("3");

        assertThat(row1).containsExactly("1", true, false);
        assertThat(row2).containsExactly("2", false, true);
        assertThat(row3).containsExactly("3", true, true);
    }
}