package de.hpi.bpt.logtransformer.transformation.transformation.multi.controlflow;

import de.hpi.bpt.logtransformer.transformation.EventLogBuilder;
import de.hpi.bpt.logtransformer.transformation.transformation.LogTransformer;
import de.hpi.bpt.logtransformer.transformation.transformation.once.controlflow.EventsTransformation;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class EventsTransformationTest {

    @Test
    void transform() {
        // Arrange
        var sourceEventLog = new EventLogBuilder()
                .schema()
                .build()
                .trace("1")
                .row(new Date(1L), "A2")
                .build()
                .trace("2")
                .row(new Date(1L), "A3")
                .row(new Date(2L), "A3")
                .row(new Date(3L), "A4")
                .row(new Date(4L), "A3")
                .build()
                .build();

        var transformation = new EventsTransformation();

        // Act
        var afterTransformation = new LogTransformer(sourceEventLog).with(transformation).transform();

        // Assert
        assertThat(afterTransformation.getSchema()).containsOnlyKeys("caseId", "#Events in case", "#Distinct events in case", "First event", "Last event");

        var row1 = afterTransformation.get("1");
        var row2 = afterTransformation.get("2");

        assertThat(row1).containsExactly("1", 1, 1, "A2", "A2");
        assertThat(row2).containsExactly("2", 4, 2, "A3", "A3");
    }

}