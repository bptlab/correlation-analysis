package de.hpi.bpt.logtransformer.transformation.operations.once.time;

import de.hpi.bpt.logtransformer.transformation.EventLogBuilder;
import de.hpi.bpt.logtransformer.transformation.operations.LogTransformer;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class ParallelCaseCountTransformationTest {

    @Test
    void transform() {
        // Arrange
        var sourceEventLog = new EventLogBuilder()
                .schema()
                .build()
                .trace("1")
                .row(new Date(100L), "A1")
                .row(new Date(200L), "A2")
                .build()
                .trace("before 1")
                .row(new Date(0L), "A1")
                .row(new Date(40L), "A2")
                .build()
                .trace("into 1")
                .row(new Date(80L), "A1")
                .row(new Date(120L), "A2")
                .build()
                .trace("inside 1")
                .row(new Date(140L), "A1")
                .row(new Date(160L), "A2")
                .build()
                .trace("out of 1")
                .row(new Date(180L), "A1")
                .row(new Date(220L), "A2")
                .build()
                .trace("after 1")
                .row(new Date(240L), "A1")
                .row(new Date(260L), "A2")
                .build()
                .build();

        var transformation = new ParallelCaseCountTransformation();

        // Act
        var afterTransformation = new LogTransformer(sourceEventLog).with(transformation).transform();

        // Assert
        assertThat(afterTransformation.getSchema()).containsOnlyKeys("caseId", "#Parallel cases");

        var row1 = afterTransformation.get("1");
        var before = afterTransformation.get("before 1");
        var into = afterTransformation.get("into 1");
        var inside = afterTransformation.get("inside 1");
        var outOf = afterTransformation.get("out of 1");
        var after = afterTransformation.get("after 1");


        assertThat(row1).containsExactly("1", 3);
        assertThat(before).containsExactly("before 1", 0);
        assertThat(into).containsExactly("into 1", 1);
        assertThat(inside).containsExactly("inside 1", 1);
        assertThat(outOf).containsExactly("out of 1", 1);
        assertThat(after).containsExactly("after 1", 0);

    }

}