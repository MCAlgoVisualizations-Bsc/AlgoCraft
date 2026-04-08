package io.github.mcalgovisualizations.visualization.models;

import io.github.mcalgovisualizations.visualization.algorithms.IAlgorithmEvent;

import java.util.List;

public abstract class AbstractContext<I> implements AlgorithmContext<I> {
    public final I values;
    public final List<IAlgorithmEvent> events;

    public AbstractContext(I values, List<IAlgorithmEvent> events) {
        this.values = values;
        this.events = events;
    }

    @Override
    public I getData() {
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
