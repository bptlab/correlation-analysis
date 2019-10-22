package de.hpi.bpt.logtransform.transformation.multi.controlflow;

import de.hpi.bpt.logtransform.EventLogBuilder;
import de.hpi.bpt.logtransform.transformation.LogTransformer;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class StageControlFlowTransformationTest {

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

        var activityToStage = Map.of("A1", "S1",
                "A2", "S2",
                "A3", "S3",
                "A4", "S3"
        );
        var transformation = new StageControlFlowTransformation(activityToStage);

        // Act
        var afterTransformation = new LogTransformer(sourceEventLog).with(transformation).transform();

        // Assert
        assertThat(afterTransformation.getSchema()).containsOnlyKeys("caseId",
                "Number of Events in 'S1'", "Times entered into 'S1'",
                "Number of Events in 'S2'", "Times entered into 'S2'",
                "Number of Events in 'S3'", "Times entered into 'S3'"
        );

        var row1 = afterTransformation.get("1");
        var row2 = afterTransformation.get("2");
        var row3 = afterTransformation.get("3");

        assertThat(row1).containsExactly("1", 1, 1, 1, 1, 2, 1);
        assertThat(row2).containsExactly("2", 3, 1, 0, 0, 0, 0);
        assertThat(row3).containsExactly("3", 2, 2, 2, 2, 0, 0);
    }

}