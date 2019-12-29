package de.hpi.bpt.logtransformer.transformation.transformation.multi.controlflow;

import de.hpi.bpt.logtransformer.transformation.EventLogBuilder;
import de.hpi.bpt.logtransformer.transformation.transformation.LogTransformer;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ActivityExecutionPathSelectionTransformationTest {

    @Test
    void transform() {
        // Arrange
        var sourceEventLog = new EventLogBuilder()
                .schema()
                .build()
                .trace("1")
                .row(new Date(1L), "A1")
                .row(new Date(2L), "A2")
                .build()
                .trace("2")
                .row(new Date(4L), "A1")
                .row(new Date(5L), "A3")
                .build()
                .trace("3")
                .row(new Date(6L), "A2")
                .row(new Date(7L), "A3")
                .build()
                .trace("4")
                .row(new Date(8L), "A1")
                .row(new Date(9L), "A2")
                .row(new Date(10L), "A3")
                .build()
                .build();

        var transformation = new ActivityExecutionPathSelectionTransformation(Set.of(
                Pair.of("A1", "A2"),
                Pair.of("A1", "A3")
        ));

        // Act
        var afterTransformation = new LogTransformer(sourceEventLog).with(transformation).transform();

        // Assert
        assertThat(afterTransformation.getSchema()).containsOnlyKeys(
                "caseId", "Choice: selected 'A2'", "Choice: selected 'A3'"
        );

        var row1 = afterTransformation.get("1");
        var row2 = afterTransformation.get("2");
        var row3 = afterTransformation.get("3");
        var row4 = afterTransformation.get("4");

        assertThat(row1).containsExactly("1", true, false);
        assertThat(row2).containsExactly("2", false, true);
        assertThat(row3).containsExactly("3", false, false);
        assertThat(row4).containsExactly("4", true, true);
    }

}