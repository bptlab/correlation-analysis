package de.hpi.bpt.datastructures;

import java.util.LinkedHashMap;

public class Schema extends LinkedHashMap<String, ColumnDefinition<?>> {

    public <T> void addColumnDefinition(String name, Class<T> type) {
        var columnDefinition = new ColumnDefinition<>(this.size(), name, type);
        this.put(name, columnDefinition);
    }


}
