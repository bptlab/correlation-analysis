package de.hpi.bpt.logtransform.transformation.multi.controlflow;

import de.hpi.bpt.logtransform.EventLogBuilder;
import de.hpi.bpt.logtransform.transformation.LogTransformer;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.Map;
import java.util.Set;

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
                .trace("4")
                .row(new Date(1000L), "A4")
                .row(new Date(2000L), "A5")
                .row(new Date(3000L), "A4")
                .row(new Date(4000L), "A5")
                .row(new Date(5000L), "A4")
                .row(new Date(6000L), "A5")
                .row(new Date(7000L), "A1")
                .row(new Date(8000L), "A4")
                .row(new Date(9000L), "A5")
                .build()
                .build();

        var activityToStage = Map.of("A1", "S1",
                "A2", "S2",
                "A3", "S3",
                "A4", "S3",
                "A5", "S4"
        );
        var parallelStages = Set.of(Pair.of("S3", "S4"));
        var transformation = new StageControlFlowTransformation(activityToStage, parallelStages);

        // Act
        var afterTransformation = new LogTransformer(sourceEventLog).with(transformation).transform();

        // Assert
        assertThat(afterTransformation.getSchema()).containsOnlyKeys("caseId",
                "#Events in 'S1'", "Times entered into 'S1'",
                "#Events in 'S2'", "Times entered into 'S2'",
                "#Events in 'S3'", "Times entered into 'S3'",
                "#Events in 'S4'", "Times entered into 'S4'"
        );

        var row1 = afterTransformation.get("1");
        var row2 = afterTransformation.get("2");
        var row3 = afterTransformation.get("3");
        var row4 = afterTransformation.get("4");

        assertThat(row1).containsExactly("1", 1, 1, 1, 1, 2, 1, 0, 0);
        assertThat(row2).containsExactly("2", 3, 1, 0, 0, 0, 0, 0, 0);
        assertThat(row3).containsExactly("3", 2, 2, 2, 2, 0, 0, 0, 0);
        assertThat(row4).containsExactly("4", 1, 1, 0, 0, 4, 2, 4, 2);
    }

}