package de.hpi.bpt.logtransform.datastructures;

import java.lang.reflect.Array;
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

    public CaseColumn(Class<T> type, int numCases) {
        this.type = type;
        values = new ArrayList<>(Arrays.asList((T[]) Array.newInstance(type, numCases)));
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

    public void setValue(int i, Object value) {
        if (value != null && !type.isInstance(value)) {
            throw new RuntimeException("Unexpected value '" + value + "' for type '" + type.getSimpleName() + "'!");
        }
        values.set(i, type.cast(value));
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
