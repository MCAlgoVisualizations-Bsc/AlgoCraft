package io.github.mcalgovisualizations.visualization.models;

import io.github.mcalgovisualizations.visualization.algorithm.IAlgorithmEvent;

import java.util.List;

/**
 * Represents the mutable execution context for an algorithm.
 *
 * <p>The context acts as both the data source and the event sink for algorithm
 * execution.</p>
 *
 * @param <V> the type of the underlying data model
 */
public interface AlgorithmContext<V> {

    /**
     * Returns the current data being processed by the algorithm.
     *
     * @return the underlying data
     */
    V getData();

    /**
     * Returns the ordered list of emitted algorithm events.
     *
     * @return ordered list of emitted events
     */
    List<IAlgorithmEvent> getEvents();

    /**
     * Emits one or more algorithm events.
     *
     * @param e the events to emit
     */
    void emit(IAlgorithmEvent... e);

    /**
     * Creates a copy of this context.
     *
     * @param <C> the concrete context type
     * @return a new, independent copy of this context
     */
    <C extends AlgorithmContext<V>> C copy();

    /**
     * Creates a copy of the underlying data.
     *
     * @return a copy of the data
     */
    V copyData();

    /**
     * Produces a randomized version of the underlying data.
     *
     * @return randomized data
     */
    V randomizeData();
}
