package de.hpi.bpt.logtransform.transformation.multi.time;

import de.hpi.bpt.logtransform.EventLogBuilder;
import de.hpi.bpt.logtransform.transformation.LogTransformer;
import de.hpi.bpt.logtransform.transformation.once.time.CaseDurationThresholdTransformation;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class CaseDurationThresholdTransformationTest {

    private static final int TO_SECONDS = 1000;
    private static final int TO_MINUTES = 60;
    private static final int TO_HOURS = 60;
    private static final int TO_DAYS = TO_SECONDS * TO_MINUTES * TO_HOURS * 24;

    @Test
    void transform() {
        // Arrange
        var sourceEventLog = new EventLogBuilder()
                .schema()
                .build()
                .trace("1")
                .row(new Date(0), "A1")
                .row(new Date(2 * TO_DAYS), "A2")
                .row(new Date(4 * TO_DAYS), "A3")
                .build()
                .trace("2")
                .row(new Date(5 * TO_DAYS), "A2")
                .row(new Date(10 * TO_DAYS), "A3")
                .build()
                .trace("3")
                .row(new Date(0), "A1")
                .row(new Date(6 * TO_DAYS), "A3")
                .build()
                .build();

        var transformation = CaseDurationThresholdTransformation.days(5);

        // Act
        var afterTransformation = new LogTransformer(sourceEventLog).with(transformation).transform();

        // Assert
        assertThat(afterTransformation.getSchema()).containsOnlyKeys("caseId", "duration_below_threshold");

        var row1 = afterTransformation.get("1");
        var row2 = afterTransformation.get("2");
        var row3 = afterTransformation.get("3");

        assertThat(row1).containsExactly("1", true);
        assertThat(row2).containsExactly("2", true);
        assertThat(row3).containsExactly("3", false);
    }

}