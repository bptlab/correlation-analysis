package de.hpi.bpt.logtransform.transformation.resource;

import de.hpi.bpt.logtransform.EventLogBuilder;
import de.hpi.bpt.logtransform.transformation.LogTransformer;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class ActivityBasedNumberOfResourcesInvolvedTransformationTest {

    @Test
    void transform() {
        // Arrange
        var sourceEventLog = new EventLogBuilder()
                .schema()
                .build()
                .trace("1")
                .row(new Date(1L), "A1")
                .build()
                .trace("2")
                .row(new Date(2L), "A1")
                .row(new Date(3L), "A2")
                .build()
                .trace("3")
                .row(new Date(4L), "A3")
                .build()
                .trace("4")
                .row(new Date(5L), "A1")
                .row(new Date(6L), "A3")
                .row(new Date(7L), "A2")
                .build()
                .build();

        var transformation = new ActivityBasedNumberOfResourcesInvolvedTransformation()
                .with("A1", "L1")
                .with("A2", "L1")
                .with("A3", "L2");

        // Act
        var afterTransformation = new LogTransformer(sourceEventLog).with(transformation).transform();

        // Assert
        assertThat(afterTransformation.getSchema()).containsOnlyKeys("caseId", "activitynumresources");

        var row1 = afterTransformation.get("1");
        var row2 = afterTransformation.get("2");
        var row3 = afterTransformation.get("3");
        var row4 = afterTransformation.get("4");

        assertThat(row1).containsExactly("1", 1);
        assertThat(row2).containsExactly("2", 1);
        assertThat(row3).containsExactly("3", 1);
        assertThat(row4).containsExactly("4", 2);
    }

}