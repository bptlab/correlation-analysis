package de.hpi.bpt.datastructures;

public class ColumnDefinition<T> {

    private final int id;
    private final String name;
    private final Class<T> type;

    public ColumnDefinition(int id, String name, Class<T> type) {
        this.id = id;
        this.name = name;
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Class<T> getType() {
        return type;
    }
}
