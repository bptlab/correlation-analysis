package de.hpi.bpt.transformation;

import de.hpi.bpt.EventLogBuilder;
import de.hpi.bpt.datastructures.CaseColumn;
import de.hpi.bpt.datastructures.CaseLog;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

public class ExistingAttributeTransformationTest {

    @Test
    public void transform_calculatesStartEndAndAggregateValues() {
        // Arrange
        var sourceEventLog = new EventLogBuilder()
                .schema()
                .column("integerTestValue", Integer.class)
                .column("stringTestValue", String.class)
                .build()
                .trace(1)
                .row(new Date(1L), "C1A1", 1, "StartValue")
                .row(new Date(2L), "C1A2", 20, "MidValue")
                .row(new Date(3L), "C1A3", 9, "EndValue")
                .build()
                .build();

        var transformation = new ExistingAttributeTransformation();

        // Act
        CaseLog afterTransformation = new LogTransformer(sourceEventLog).with(transformation).transform();

        // Assert
        CaseColumn<Integer> integerTestValueStart = afterTransformation.getTyped("integerTestValue_start");
        CaseColumn<Integer> integerTestValueEnd = afterTransformation.getTyped("integerTestValue_end");
        CaseColumn<Integer> integerTestValueMax = afterTransformation.getTyped("integerTestValue_max");
        CaseColumn<Integer> integerTestValueMin = afterTransformation.getTyped("integerTestValue_min");
        CaseColumn<Double> integerTestValueAvg = afterTransformation.getTyped("integerTestValue_avg");
        CaseColumn<String> stringTestValueStart = afterTransformation.getTyped("stringTestValue_start");
        CaseColumn<String> stringTestValueEnd = afterTransformation.getTyped("stringTestValue_end");

        assertThat(integerTestValueStart.get(0)).isEqualTo(1);
        assertThat(integerTestValueEnd.get(0)).isEqualTo(9);
        assertThat(integerTestValueMax.get(0)).isEqualTo(20);
        assertThat(integerTestValueMin.get(0)).isEqualTo(1);
        assertThat(integerTestValueAvg.get(0)).isEqualTo(10D);
        assertThat(stringTestValueStart.get(0)).isEqualTo("StartValue");
        assertThat(stringTestValueEnd.get(0)).isEqualTo("EndValue");
    }
}