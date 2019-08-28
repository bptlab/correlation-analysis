package de.hpi.bpt.logtransform.transformation.time;

import de.hpi.bpt.logtransform.EventLogBuilder;
import de.hpi.bpt.logtransform.transformation.LogTransformer;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class CaseEndTimeTransformationTest {

    @Test
    void transform() {
        // Arrange
        var sourceEventLog = new EventLogBuilder()
                .schema()
                .build()
                .trace("1")
                .row(new Date(0), "A1")
                .row(new Date(100), "A2")
                .build()
                .trace("2")
                .row(new Date(0), "A1")
                .row(new Date(100), "A2")
                .row(new Date(200), "A3")
                .build()
                .build();

        var transformation = new CaseEndTimeTransformation();

        // Act
        var afterTransformation = new LogTransformer(sourceEventLog).with(transformation).transform();

        // Assert
        assertThat(afterTransformation.getSchema()).containsOnlyKeys("caseId", "caseend");

        assertThat(afterTransformation.get("1")).containsExactly("1", new Date(100));
        assertThat(afterTransformation.get("2")).containsExactly("2", new Date(200));
    }

}