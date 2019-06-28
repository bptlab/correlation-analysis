package de.hpi.bpt.transformation;

import de.hpi.bpt.EventLogBuilder;
import de.hpi.bpt.datastructures.CaseColumn;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class ActivityAppearanceTransformationTest {

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
                .row(new Date(4L), "A2")
                .row(new Date(5L), "A3")
                .build()
                .trace("3")
                .row(new Date(6L), "A3")
                .build()
                .build();

        var transformation = new ActivityAppearanceTransformation("A1", "A2", "A3");

        // Act
        var afterTransformation = new LogTransformer(sourceEventLog).with(transformation).transform();

        // Assert
        CaseColumn<Boolean> a1Appearance = afterTransformation.getTyped("A1_appearance");
        CaseColumn<Boolean> a2Appearance = afterTransformation.getTyped("A2_appearance");
        CaseColumn<Boolean> a3Appearance = afterTransformation.getTyped("A3_appearance");

        assertThat(a1Appearance.getValues()).containsExactly(true, false, false);
        assertThat(a2Appearance.getValues()).containsExactly(true, true, false);
        assertThat(a3Appearance.getValues()).containsExactly(true, true, true);
    }

}