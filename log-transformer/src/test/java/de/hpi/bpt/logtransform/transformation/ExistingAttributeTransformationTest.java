package de.hpi.bpt.logtransform.transformation;

import de.hpi.bpt.logtransform.EventLogBuilder;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class ExistingAttributeTransformationTest {

    @Test
    void transform() {
        // Arrange
        var sourceEventLog = new EventLogBuilder()
                .schema()
                .column("integerTestValue", Integer.class)
                .column("stringTestValue", String.class)
                .build()
                .trace("1")
                .row(new Date(1L), "C1A1", 1, "StartValue")
                .row(new Date(2L), "C1A1", 20, "MidValue")
                .row(new Date(3L), "C1A2", 9, "EndValue")
                .build()
                .build();

        var transformation = new ExistingAttributeTransformation();

        // Act
        var afterTransformation = new LogTransformer(sourceEventLog).with(transformation).transform();

        // Assert
        assertThat(afterTransformation.getSchema()).containsOnlyKeys("caseId", "activity_start", "activity_end", "activity_unique", "integerTestValue_start", "integerTestValue_end", "integerTestValue_unique", "integerTestValue_max", "integerTestValue_min", "integerTestValue_avg", "stringTestValue_start", "stringTestValue_end", "stringTestValue_unique");

        var row1 = afterTransformation.get("1");

        assertThat(row1).containsExactly("1", "C1A1", "C1A2", 2, 1, 9, 3, 20, 1, 10D, "StartValue", "EndValue", 3);
    }
}