package de.hpi.bpt.logtransform.transformation.controlflow;

import de.hpi.bpt.logtransform.EventLogBuilder;
import de.hpi.bpt.logtransform.transformation.LogTransformer;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class SubProcessTransformationTest {

    @Test
    void transform() {
        // Arrange
        var sourceEventLog = new EventLogBuilder()
                .schema()
                .build()
                .trace("1")
                .row(new Date(1000L), "A1")
                .row(new Date(2000L), "A2")
                .row(new Date(3000L), "A3")
                .row(new Date(4000L), "A4")
                .build()
                .trace("2")
                .row(new Date(1000L), "A1")
                .row(new Date(2000L), "A1")
                .row(new Date(3000L), "A1")
                .build()
                .trace("3")
                .row(new Date(1000L), "A1")
                .row(new Date(2000L), "A2")
                .row(new Date(3000L), "A1")
                .row(new Date(4000L), "A2")
                .build()
                .build();

        var transformation = new SubProcessTransformation()
                .with("Sub1", "A1")
                .with("Sub2", "A2")
                .with("Sub3", "A3", "A4");

        // Act
        var afterTransformation = new LogTransformer(sourceEventLog).with(transformation).transform();

        // Assert
        assertThat(afterTransformation.getSchema()).containsOnlyKeys("caseId",
                "Number of Events in 'Sub1'", "Time spent in 'Sub1' in minutes", "Times entered into 'Sub1'",
                "Number of Events in 'Sub2'", "Time spent in 'Sub2' in minutes", "Times entered into 'Sub2'",
                "Number of Events in 'Sub3'", "Time spent in 'Sub3' in minutes", "Times entered into 'Sub3'"
        );

        var row1 = afterTransformation.get("1");
        var row2 = afterTransformation.get("2");
        var row3 = afterTransformation.get("3");

        assertThat(row1).containsExactly("1", 1, 1, 1, 1, 1, 1, 2, 1, 1);
        assertThat(row2).containsExactly("2", 3, 2, 1, 0, 0, 0, 0, 0, 0);
        assertThat(row3).containsExactly("3", 2, 2, 2, 2, 1, 2, 0, 0, 0);
    }

}