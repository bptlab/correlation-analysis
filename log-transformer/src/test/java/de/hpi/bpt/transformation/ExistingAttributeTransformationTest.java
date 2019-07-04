package de.hpi.bpt.transformation;

import de.hpi.bpt.EventLogBuilder;
import de.hpi.bpt.datastructures.CaseColumn;
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
                .row(new Date(2L), "C1A2", 20, "MidValue")
                .row(new Date(3L), "C1A3", 9, "EndValue")
                .build()
                .build();

        var transformation = new ExistingAttributeTransformation();

        // Act
        var afterTransformation = new LogTransformer(sourceEventLog).with(transformation).transform();

        // Assert
        assertThat(afterTransformation.getSchema()).containsOnlyKeys("caseId", "activity_start", "activity_end", "integerTestValue_start", "integerTestValue_end", "integerTestValue_max", "integerTestValue_min", "integerTestValue_avg", "stringTestValue_start", "stringTestValue_end");

        CaseColumn<String> activityStart = afterTransformation.getTyped("activity_start");
        CaseColumn<String> activityEnd = afterTransformation.getTyped("activity_end");
        CaseColumn<Integer> integerTestValueStart = afterTransformation.getTyped("integerTestValue_start");
        CaseColumn<Integer> integerTestValueEnd = afterTransformation.getTyped("integerTestValue_end");
        CaseColumn<Integer> integerTestValueMax = afterTransformation.getTyped("integerTestValue_max");
        CaseColumn<Integer> integerTestValueMin = afterTransformation.getTyped("integerTestValue_min");
        CaseColumn<Double> integerTestValueAvg = afterTransformation.getTyped("integerTestValue_avg");
        CaseColumn<String> stringTestValueStart = afterTransformation.getTyped("stringTestValue_start");
        CaseColumn<String> stringTestValueEnd = afterTransformation.getTyped("stringTestValue_end");

        assertThat(activityStart.getValues()).containsExactly("C1A1");
        assertThat(activityEnd.getValues()).containsExactly("C1A3");
        assertThat(integerTestValueStart.getValues()).containsExactly(1);
        assertThat(integerTestValueEnd.getValues()).containsExactly(9);
        assertThat(integerTestValueMax.getValues()).containsExactly(20);
        assertThat(integerTestValueMin.getValues()).containsExactly(1);
        assertThat(integerTestValueAvg.getValues()).containsExactly(10D);
        assertThat(stringTestValueStart.getValues()).containsExactly("StartValue");
        assertThat(stringTestValueEnd.getValues()).containsExactly("EndValue");
    }
}