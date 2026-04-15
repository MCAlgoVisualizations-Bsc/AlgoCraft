package io.github.mcalgovisualizations.visualization.models;

import io.github.mcalgovisualizations.visualization.algorithm.IAlgorithmEvent;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractContext<T> implements AlgorithmContext<T> {
    public T values;
    public final List<IAlgorithmEvent> events = new ArrayList<>();

    public AbstractContext(T values) {
        this.values = values;
    }

    @Override
    public T getData() {
        return values;
    }

    @Override
    public void emit(IAlgorithmEvent e) {
        events.add(e);
    }

    @Override
    public List<IAlgorithmEvent> getEvents() {
        return List.copyOf(events);
    }
}
