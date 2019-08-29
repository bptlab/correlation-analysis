package de.hpi.bpt.logtransform.transformation.resource;

import de.hpi.bpt.logtransform.EventLogBuilder;
import de.hpi.bpt.logtransform.transformation.LogTransformer;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class ActivityBasedHandoverCountTransformationTest {

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

        var transformation = new ActivityBasedHandoverCountTransformation()
                .with("A1", "A3")
                .with("A1", "A2")
                .with("A2", "A3");

        // Act
        var afterTransformation = new LogTransformer(sourceEventLog).with(transformation).transform();

        // Assert
        assertThat(afterTransformation.getSchema()).containsOnlyKeys("caseId", "activityhandovercount");

        var row1 = afterTransformation.get("1");
        var row2 = afterTransformation.get("2");
        var row3 = afterTransformation.get("3");

        assertThat(row1).containsExactly("1", 2);
        assertThat(row2).containsExactly("2", 2);
        assertThat(row3).containsExactly("3", 3);
    }
}