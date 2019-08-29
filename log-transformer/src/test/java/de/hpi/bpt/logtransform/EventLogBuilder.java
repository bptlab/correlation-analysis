package de.hpi.bpt.logtransform;

import de.hpi.bpt.logtransform.datastructures.ColumnEventLog;
import de.hpi.bpt.logtransform.datastructures.LogColumn;
import de.hpi.bpt.logtransform.datastructures.Schema;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

public class EventLogBuilder {

    public SchemaBuilder schema() {
        return new SchemaBuilder();
    }

    public class SchemaBuilder {

        private final Schema schema;

        private SchemaBuilder() {
            schema = new Schema();
            schema.addColumnDefinition("caseId", String.class);
            schema.setCaseIdName("caseId");
            schema.addColumnDefinition("timestamp", Date.class);
            schema.setTimestampName("timestamp");
            schema.addColumnDefinition("activity", String.class);
            schema.setActivityName("activity");
        }

        public SchemaBuilder withResourceColumn() {
            schema.addColumnDefinition("resource", String.class);
            schema.setResourceName("resource");
            return this;
        }

        public SchemaBuilder column(String name, Class<?> type) {
            schema.addColumnDefinition(name, type);
            return this;
        }

        public ContentBuilder build() {
            return new ContentBuilder(schema);
        }
    }

    public class ContentBuilder {

        private final Schema schema;
        private final List<LogColumn<?>> columns;

        private ContentBuilder(Schema schema) {
            this.schema = schema;
            this.columns = new ArrayList<>();
            for (var columnDefinition : schema.values()) {
                columns.add(columnDefinition.getId(), new LogColumn<>(columnDefinition.getType()));
            }
        }

        public TraceBuilder trace(String caseId) {
            return new TraceBuilder(this, caseId, columns);
        }

        public ColumnEventLog build() {
            var map = new LinkedHashMap<String, LogColumn<?>>();
            schema.forEach((key, value) -> map.put(key, columns.get(value.getId())));
            return new ColumnEventLog("TestLog", schema, map);
        }
    }

    public class TraceBuilder {

        private final ContentBuilder contentBuilder;
        private final String caseId;
        private final List<LogColumn<?>> columns;

        private TraceBuilder(ContentBuilder contentBuilder, String caseId, List<LogColumn<?>> columns) {
            this.contentBuilder = contentBuilder;
            this.caseId = caseId;
            this.columns = columns;

            columns.forEach(LogColumn::addNewTrace);
        }

        public TraceBuilder row(Date timestamp, String activityName, Object... additionalFields) {
            columns.get(0).addValue(caseId);
            columns.get(1).addValue(timestamp);
            columns.get(2).addValue(activityName);
            for (int i = 3; i < additionalFields.length + 3; i++) {
                columns.get(i).addValue(additionalFields[i - 3]);
            }
            return this;
        }

        public ContentBuilder build() {
            return contentBuilder;
        }
    }
}
