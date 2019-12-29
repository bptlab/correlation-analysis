package de.hpi.bpt.logtransformer.transformation.operations.multi.time;

import de.hpi.bpt.logtransformer.transformation.EventLogBuilder;
import de.hpi.bpt.logtransformer.transformation.operations.LogTransformer;
import org.junit.jupiter.api.Test;

import java.text.SimpleDateFormat;

import static org.assertj.core.api.Assertions.assertThat;

class BetweenEventsDurationTransformationTest {

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

        var transformation = new BetweenEventsDurationTransformation();

        // Act
        var afterTransformation = new LogTransformer(sourceEventLog).with(transformation).transform();

        // Assert
        assertThat(afterTransformation.getSchema()).containsOnlyKeys("caseId",
                "Duration between 'A1' and 'A2' (in minutes)",
                "Duration between 'A1' and 'A3' (in minutes)",
                "Duration between 'A2' and 'A1' (in minutes)",
                "Duration between 'A2' and 'A3' (in minutes)",
                "Duration between 'A3' and 'A1' (in minutes)",
                "Duration between 'A3' and 'A2' (in minutes)"
        );

        var row1 = afterTransformation.get("1");

        assertThat(row1).containsExactly("1", 0, 0, 0, 15, 0, 0);
    }

}