package de.hpi.bpt.logtransform.transformation.multi.time;

import de.hpi.bpt.logtransform.EventLogBuilder;
import de.hpi.bpt.logtransform.transformation.LogTransformer;
import org.junit.jupiter.api.Test;

import java.text.SimpleDateFormat;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class BetweenStagesDurationTransformationTest {

    @Test
    void transform() throws Exception {
        // Arrange
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        var sourceEventLog = new EventLogBuilder()
                .schema()
                .build()
                .trace("1")
                .row(formatter.parse("01-01-2000 09:00"), "A1")
                .row(formatter.parse("01-01-2000 09:15"), "A2")
                .row(formatter.parse("01-01-2000 09:30"), "A1")
                .row(formatter.parse("01-01-2000 09:45"), "A3")
                .build()
                .build();

        var activityToStage = Map.of("A1", "S1",
                "A2", "S2",
                "A3", "S2"
        );
        var transformation = new BetweenStagesDurationTransformation(activityToStage);

        // Act
        var afterTransformation = new LogTransformer(sourceEventLog).with(transformation).transform();

        // Assert
        assertThat(afterTransformation.getSchema()).containsOnlyKeys(
                "caseId",
                "Duration between 'S1' and 'S2' (in minutes)",
                "Duration between 'S2' and 'S1' (in minutes)"
        );

        assertThat(afterTransformation.get("1")).containsExactly("1", 15, 15);

    }
}