package io.github.mcalgovisualizations.visualization.models;

import io.github.mcalgovisualizations.visualization.algorithm.IAlgorithmEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Base implementation of {@link AlgorithmContext}.
 *
 * <p>This class stores the algorithm data and collects emitted
 * {@link IAlgorithmEvent}s in insertion order.</p>
 *
 * <p>{@link #getEvents()} returns a defensive copy, so callers cannot mutate the
 * internal event history directly.</p>
 *
 * @param <T> the type of data stored by this context
 */
public abstract class AbstractContext<T> implements AlgorithmContext<T> {

    /**
     * The mutable data operated on by the algorithm.
     */
    public T values;

    /**
     * Ordered history of events emitted during algorithm execution.
     */
    public final List<IAlgorithmEvent> events = new ArrayList<>();

    /**
     * Creates a new context with the given data.
     *
     * @param values the initial algorithm data
     */
    public AbstractContext(T values) {
        this.values = values;
    }

    /**
     * Returns the current algorithm data.
     *
     * @return the current data
     */
    @Override
    public T getData() {
        return values;
    }

    /**
     * Appends one or more events to the event history.
     *
     * @param e events to append
     */
    @Override
    public void emit(IAlgorithmEvent... e) {
        events.addAll(Arrays.asList(e));
    }

    /**
     * Returns a snapshot of the emitted event history.
     *
     * @return immutable copy of the emitted events
     */
    @Override
    public List<IAlgorithmEvent> getEvents() {
        return List.copyOf(events);
    }
}
