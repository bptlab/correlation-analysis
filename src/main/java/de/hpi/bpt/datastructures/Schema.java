package de.hpi.bpt.datastructures;

import java.util.ArrayList;

public class Schema extends ArrayList<ColumnDefinition<?>> {

    public <T> void addColumnDefinition(String name, Class<T> type) {
        var columnDefinition = new ColumnDefinition<>(this.size(), name, type);
        this.add(columnDefinition);
    }

    public ColumnDefinition<?> get(String columnName) {
        for (ColumnDefinition<?> columnDefinition : this) {
            if (columnDefinition.getName().equals(columnName)) {
                return columnDefinition;
            }
        }
        throw new RuntimeException("No column with name '" + columnName + "' found!");
    }
}
