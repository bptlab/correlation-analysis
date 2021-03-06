package de.hpi.bpt.logtransformer.transformation.datastructures;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CaseColumn<T> {

    private final Class<T> type;
    private List<T> values;

    public CaseColumn(Class<T> type) {
        this.type = type;
        values = new ArrayList<>();
    }

    public CaseColumn(Class<T> type, T[] values) {
        this.type = type;
        this.values = new ArrayList<>(Arrays.asList(values));
    }

    public List<T> getValues() {
        return values;
    }

    public Class<T> getType() {
        return type;
    }

    public T get(int i) {
        return values.get(i);
    }

    public int size() {
        return values.size();
    }

    public void addValue(Object value) {
        if (value != null && !type.isInstance(value)) {
            throw new RuntimeException("Unexpected value '" + value + "' for type '" + type.getSimpleName() + "'!");
        }
        values.add(type.cast(value));
    }

    public int indexOf(T value) {
        return values.indexOf(value);
    }

    public <U> CaseColumn<U> as(Class<U> type) {
        if (!this.type.equals(type)) {
            throw new RuntimeException("Column is of type '" + this.type.getSimpleName() + "', but type '" + type.getSimpleName() + "' was requested!");

        }
        return (CaseColumn<U>) this;
    }
}
