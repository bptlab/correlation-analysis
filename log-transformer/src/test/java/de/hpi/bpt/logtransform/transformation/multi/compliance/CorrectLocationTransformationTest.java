package de.hpi.bpt.logtransform.transformation.multi.compliance;

import de.hpi.bpt.logtransform.EventLogBuilder;
import de.hpi.bpt.logtransform.transformation.LogTransformer;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CorrectLocationTransformationTest {

    @Test
    void transform() {
        // Arrange
        var sourceEventLog = new EventLogBuilder()
                .schema()
                .build()
                .trace("1")
                .row(new Date(1L), "A1")
                .row(new Date(2L), "A2")
                .row(new Date(3L), "A4")
                .build()
                .trace("2")
                .row(new Date(4L), "A1")
                .row(new Date(5L), "A3")
                .build()
                .trace("3")
                .row(new Date(6L), "A4")
                .build()
                .trace("4")
                .row(new Date(7L), "A1")
                .row(new Date(8L), "A4")
                .build()
                .trace("5")
                .row(new Date(9L), "A4")
                .row(new Date(10L), "A3")
                .row(new Date(11L), "A2")
                .row(new Date(12L), "A1")
                .build()
                .trace("6")
                .row(new Date(13L), "Unknown")
                .row(new Date(14L), "A1")
                .row(new Date(15L), "Unknown")
                .build()
                .build();

        var compliantTransitions = Map.of("#START#", List.of("A1"),
                "A1", List.of("A2", "A3"),
                "A2", List.of("A4"),
                "A3", List.of("A4"),
                "A4", List.of("#END#"));
        var activityToStage = Map.of(
                "A1", "S1",
                "A2", "S2",
                "A3", "S3",
                "A4", "S3"
        );
        var transformation = new CorrectLocationTransformation(compliantTransitions, activityToStage);

        // Act
        var afterTransformation = new LogTransformer(sourceEventLog).with(transformation).transform();

        // Assert
        assertThat(afterTransformation.getSchema()).containsOnlyKeys("caseId", "S1 - Correct predecessor", "S1 - Correct successor", "S2 - Correct predecessor", "S2 - Correct successor", "S3 - Correct predecessor", "S3 - Correct successor");

        var row1 = afterTransformation.get("1");
        var row2 = afterTransformation.get("2");
        var row3 = afterTransformation.get("3");
        var row4 = afterTransformation.get("4");
        var row5 = afterTransformation.get("5");
        var row6 = afterTransformation.get("6");

        assertThat(row1).containsExactly("1", true, true, true, true, true, true);
        assertThat(row2).containsExactly("2", true, true, null, null, true, true);
        assertThat(row3).containsExactly("3", null, null, null, null, false, true);
        assertThat(row4).containsExactly("4", true, true, null, null, true, true);
        assertThat(row5).containsExactly("5", false, false, false, false, false, false);
        assertThat(row6).containsExactly("6", true, true, null, null, null, null);
    }

}