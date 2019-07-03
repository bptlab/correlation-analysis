package de.hpi.bpt.datastructures;

import java.util.ArrayList;
import java.util.List;

public class LogColumn<T> {

    private final Class<T> type;
    private List<List<T>> traces = new ArrayList<>();

    public LogColumn(Class<T> type) {
        this.type = type;
    }

    public List<List<T>> getTraces() {
        return traces;
    }

    public int numTraces() {
        return traces.size();
    }

    public List<T> get(int i) {
        return traces.get(i);
    }

    public List<T> getLastTrace() {
        return traces.get(traces.size() - 1);
    }

    public void addNewTrace() {
        traces.add(new ArrayList<>());
    }

    public void addValue(Object value) {
        if (value != null && !type.isInstance(value)) {
            throw new RuntimeException("Unexpected value '" + value + "' for type '" + type.getSimpleName() + "'!");
        }
        getLastTrace().add(type.cast(value));
    }

    public Class<T> getType() {
        return type;
    }

    public <U> LogColumn<U> as(Class<U> type) {
        if (!this.type.equals(type)) {
            throw new RuntimeException("Column is of type '" + this.type.getSimpleName() + "', but type '" + type.getSimpleName() + "' was requested!");

        }
        return (LogColumn<U>) this;
    }
}
