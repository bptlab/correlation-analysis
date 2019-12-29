package de.hpi.bpt.logtransformer.transformation.transformation.once.resource;

import de.hpi.bpt.logtransformer.transformation.EventLogBuilder;
import de.hpi.bpt.logtransformer.transformation.transformation.LogTransformer;
import de.hpi.bpt.logtransformer.transformation.transformation.once.resource.DepartmentHandoverCountTransformation;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class DepartmentHandoverCountTransformationTest {

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
                .row(new Date(5L), "A1")
                .row(new Date(6L), "A1")
                .row(new Date(7L), "A3")
                .build()
                .build();

        var activityToLane = Map.of(
                "A1", "L1",
                "A2", "L2",
                "A3", "L3"
        );

        var transformation = new DepartmentHandoverCountTransformation(activityToLane);

        // Act
        var afterTransformation = new LogTransformer(sourceEventLog).with(transformation).transform();

        // Assert
        assertThat(afterTransformation.getSchema()).containsOnlyKeys("caseId", "#Handovers (between departments)");

        assertThat(afterTransformation.get("1")).containsExactly("1", 2);
        assertThat(afterTransformation.get("2")).containsExactly("2", 1);
    }

}