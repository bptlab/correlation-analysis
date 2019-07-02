package de.hpi.bpt.io;

import de.hpi.bpt.datastructures.ColumnDefinition;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class CsvEventLogReaderTest {

    @Test
    void read_simpleFile() {
        // Arrange
        var csvLogReader = new CsvLogReader();
        var file = new File(this.getClass().getResource("simple.csv").getFile());
        var dateFormat = new SimpleDateFormat(csvLogReader.getDateFormat());
        var eventLogReader = new CsvEventLogReader(csvLogReader);

        // Act
        var eventLog = eventLogReader.read(file);

        // Assert
        var schema = eventLog.getSchema();
        assertThat(schema.get("caseid")).isEqualToComparingFieldByField(new ColumnDefinition<>(0, "caseid", String.class));
        assertThat(schema.get("timestamp")).isEqualToComparingFieldByField(new ColumnDefinition<>(1, "timestamp", Date.class));
        assertThat(schema.get("activity")).isEqualToComparingFieldByField(new ColumnDefinition<>(2, "activity", String.class));
        assertThat(schema.get("customername")).isEqualToComparingFieldByField(new ColumnDefinition<>(3, "customername", String.class));
        assertThat(schema.get("isnew")).isEqualToComparingFieldByField(new ColumnDefinition<>(4, "isnew", Boolean.class));
        assertThat(schema.get("discount")).isEqualToComparingFieldByField(new ColumnDefinition<>(5, "discount", Double.class));

        assertThat(eventLog.get("caseid").getType()).isEqualTo(String.class);
        assertThat(eventLog.getTyped("caseid", String.class).getTraces())
                .flatExtracting(traceList -> traceList)
                .containsExactly("0", "0", "1");

        assertThat(eventLog.get("timestamp").getType()).isEqualTo(Date.class);
        assertThat(eventLog.getTyped("timestamp", Date.class).getTraces())
                .flatExtracting(traceList -> traceList)
                .extracting(dateFormat::format)
                .containsExactly("2005-01-03T00:14:24+01:00", "2005-01-03T00:15:33+01:00", "2005-01-03T00:16:06+01:00");

        assertThat(eventLog.get("activity").getType()).isEqualTo(String.class);
        assertThat(eventLog.getTyped("activity", String.class).getTraces())
                .flatExtracting(traceList -> traceList)
                .containsExactly("register customer", "print card", "create invoice");

        assertThat(eventLog.get("customername").getType()).isEqualTo(String.class);
        assertThat(eventLog.getTyped("customername", String.class).getTraces())
                .flatExtracting(traceList -> traceList)
                .containsExactly("muller", "muller", "smith");

        assertThat(eventLog.get("isnew").getType()).isEqualTo(Boolean.class);
        assertThat(eventLog.getTyped("isnew", Boolean.class).getTraces())
                .flatExtracting(traceList -> traceList)
                .containsExactly(true, true, false);

        assertThat(eventLog.get("discount").getType()).isEqualTo(Double.class);
        assertThat(eventLog.getTyped("discount", Double.class).getTraces())
                .flatExtracting(traceList -> traceList)
                .containsExactly(0.0D, 0.0D, 20.5D);
    }
}