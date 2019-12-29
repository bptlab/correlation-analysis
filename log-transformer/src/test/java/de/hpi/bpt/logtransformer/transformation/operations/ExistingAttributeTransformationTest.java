package de.hpi.bpt.logtransformer.transformation.operations;

import de.hpi.bpt.logtransformer.transformation.EventLogBuilder;
import de.hpi.bpt.logtransformer.transformation.operations.multi.data.ExistingAttributeTransformation;
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
                .column("doubleTestValue", Double.class)
                .column("stringTestValue", String.class)
                .column("booleanTestValue", Boolean.class)
                .build()
                .trace("1")
                .row(new Date(1L), "C1A1", 1, 1.3, "StartValue", false)
                .row(new Date(2L), "C1A1", 20, 20.3, "MidValue", false)
                .row(new Date(3L), "C1A2", 9, 9.3, "EndValue", true)
                .build()
                .trace("2")
                .row(new Date(1L), "C1A1", 1, 1.5, "StartValue", true)
                .build()
                .trace("3")
                .row(new Date(1L), "C1A1", 1, 1.5, "EndValue", false)
                .build()
                .build();

        var transformation = new ExistingAttributeTransformation();

        // Act
        var afterTransformation = new LogTransformer(sourceEventLog).with(transformation).transform();

        // Assert
        assertThat(afterTransformation.getSchema()).containsOnlyKeys("caseId",
                "integerTestValue (at start)", "integerTestValue (at end)", "integerTestValue (#distinct values)", "integerTestValue (max)", "integerTestValue (min)", "integerTestValue (avg)",
                "doubleTestValue (at start)", "doubleTestValue (at end)", "doubleTestValue (#distinct values)", "doubleTestValue (max)", "doubleTestValue (min)", "doubleTestValue (avg)",
                "stringTestValue (at start)", "stringTestValue (at end)", "stringTestValue (#distinct values)", "stringTestValue = 'EndValue' (times present)", "stringTestValue = 'MidValue' (times present)", "stringTestValue = 'StartValue' (times present)",
                "booleanTestValue (at start)", "booleanTestValue (at end)", "booleanTestValue (#distinct values)", "booleanTestValue = 'true' (times present)", "booleanTestValue = 'false' (times present)"

        );

        var row1 = afterTransformation.get("1");
        var row2 = afterTransformation.get("2");
        var row3 = afterTransformation.get("3");

        assertThat(row1).containsExactly("1",
                1, 9, 3, 20, 1, 10D,
                1.3, 9.3, 3, 20.3, 1.3, 10.3D,
                "StartValue", "EndValue", 3, 1, 1, 1,
                false, true, 2, 1, 2
        );
        assertThat(row2).containsExactly("2",
                1, 1, 1, 1, 1, 1D,
                1.5, 1.5, 1, 1.5, 1.5, 1.5,
                "StartValue", "StartValue", 1, 0, 0, 1,
                true, true, 1, 1, 0
        );
        assertThat(row3).containsExactly("3",
                1, 1, 1, 1, 1, 1D,
                1.5, 1.5, 1, 1.5, 1.5, 1.5,
                "EndValue", "EndValue", 1, 1, 0, 0,
                false, false, 1, 0, 1
        );
    }
}