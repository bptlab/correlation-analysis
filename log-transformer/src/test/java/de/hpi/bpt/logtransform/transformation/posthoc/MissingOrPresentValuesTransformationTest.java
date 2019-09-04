package de.hpi.bpt.logtransform.transformation.posthoc;

import de.hpi.bpt.logtransform.datastructures.RowCaseLog;
import de.hpi.bpt.logtransform.datastructures.Schema;
import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

class MissingOrPresentValuesTransformationTest {

    @Test
    void transform() {
        // Arrange
        var schema = new Schema();
        schema.addColumnDefinition("caseId", String.class);
        schema.addColumnDefinition("attr1", String.class);
        schema.addColumnDefinition("attr2", Integer.class);

        var rowCaseLog = new RowCaseLog("Test", schema);
        rowCaseLog.put("1", Stream.of("1", "aValue", 1).collect(toList()));
        rowCaseLog.put("2", Stream.of("2", "", 0).collect(toList()));
        rowCaseLog.put("3", Stream.of("3", null, null).collect(toList()));

        // Act
        new MissingOrPresentValuesTransformation().transform(rowCaseLog);

        // Assert
        assertThat(rowCaseLog.getSchema()).containsOnlyKeys("caseId", "attr1", "attr2", "caseId_ispresent", "attr1_ispresent", "attr2_ispresent");
        assertThat(rowCaseLog.get("1")).containsExactly("1", "aValue", 1, true, true, true);
        assertThat(rowCaseLog.get("2")).containsExactly("2", "", 0, true, true, true);
        assertThat(rowCaseLog.get("3")).containsExactly("3", null, null, true, false, false);
    }

}