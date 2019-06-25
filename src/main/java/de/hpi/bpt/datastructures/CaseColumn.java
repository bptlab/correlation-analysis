package de.hpi.bpt.datastructures;

import java.util.ArrayList;
import java.util.List;

public class CaseColumn<T> {

    private final Class<T> type;
    List<T> values = new ArrayList<>();

    public CaseColumn(Class<T> type) {
        this.type = type;
    }

    public List<T> getValues() {
        return values;
    }

    public T get(int i) {
        return values.get(i);
    }

    public void addValue(Object value) {
        if (!type.isInstance(value)) {
            throw new RuntimeException("Unexpected value '" + value + "' for type '" + type.getSimpleName() + "'!");
        }
        values.add(type.cast(value));
    }
}
