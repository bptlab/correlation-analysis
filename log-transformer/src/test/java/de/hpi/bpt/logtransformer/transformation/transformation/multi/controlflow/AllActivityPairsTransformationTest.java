package de.hpi.bpt.logtransformer.transformation.transformation.multi.controlflow;

import de.hpi.bpt.logtransformer.transformation.EventLogBuilder;
import de.hpi.bpt.logtransformer.transformation.transformation.LogTransformer;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class AllActivityPairsTransformationTest {

    @Test
    void transform() {
        // Arrange
        var sourceEventLog = new EventLogBuilder()
                .schema()
                .build()
                .trace("1")
                .row(new Date(1L), "A1")
                .row(new Date(2L), "A2")
                .row(new Date(3L), "A3")
                .build()
                .trace("2")
                .row(new Date(4L), "A1")
                .row(new Date(5L), "A3")
                .build()
                .trace("3")
                .row(new Date(6L), "A3")
                .build()
                .build();

        var transformation = new AllActivityPairsTransformation();

        // Act
        var afterTransformation = new LogTransformer(sourceEventLog).with(transformation).transform();

        // Assert
        assertThat(afterTransformation.getSchema()).containsOnlyKeys(
                "caseId",
                "A1_A1_directflow", "A1_A1_indirectflow",
                "A1_A2_directflow", "A1_A2_indirectflow",
                "A1_A3_directflow", "A1_A3_indirectflow",
                "A2_A1_directflow", "A2_A1_indirectflow",
                "A2_A2_directflow", "A2_A2_indirectflow",
                "A2_A3_directflow", "A2_A3_indirectflow",
                "A3_A1_directflow", "A3_A1_indirectflow",
                "A3_A2_directflow", "A3_A2_indirectflow",
                "A3_A3_directflow", "A3_A3_indirectflow"
        );

        var row1 = afterTransformation.get("1");
        var row2 = afterTransformation.get("2");
        var row3 = afterTransformation.get("3");

        assertThat(row1).containsExactly("1", false, false, true, true, false, true, false, false, false, false, true, true, false, false, false, false, false, false);
        assertThat(row2).containsExactly("2", false, false, false, false, true, true, false, false, false, false, false, false, false, false, false, false, false, false);
        assertThat(row3).containsExactly("3", false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false);
    }
}