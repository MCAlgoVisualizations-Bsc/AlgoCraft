package io.github.mcalgovisualizations.visualization.models;

import io.github.mcalgovisualizations.visualization.algorithm.IAlgorithmEvent;

import java.util.List;

/**
 * Represents the execution context for an algorithm.
 *
 * <p>An {@code AlgorithmContext} encapsulates:</p>
 * <ul>
 *     <li>the underlying data being operated on</li>
 *     <li>a history of emitted {@link IAlgorithmEvent}s</li>
 *     <li>mechanisms for emitting events during execution</li>
 * </ul>
 *
 * <p>The context acts as both the data source and the event sink for an algorithm.</p>
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
     * Returns the list of emitted algorithm events.
     *
     * <p>The returned list represents the execution history in order.</p>
     *
     * @return ordered list of emitted events
     */
    List<IAlgorithmEvent> getEvents();

    /**
     * Emits one or more algorithm events.
     *
     * <p>Events are appended to the internal history and are expected to represent
     * discrete, replayable steps of the algorithm.</p>
     *
     * @param e the events to emit
     */
    void emit(IAlgorithmEvent... e);

    /**
     * Creates a copy of this context.
     *
     * <p>The copy should include a copy of the data and a separate event history.
     * Mutating the returned context must not affect the original.</p>
     *
     * @param <C> the concrete context type
     * @return a new, independent copy of this context
     */
    <C extends AlgorithmContext<V>> C copy();

    /**
     * Creates a copy of the underlying data.
     *
     * <p>The returned data must be independent from the original.</p>
     *
     * @return a copy of the data
     */
    V copyData();

    /**
     * Produces a randomized version of the underlying data.
     *
     * <p>This may mutate the current context's data or return a new instance,
     * depending on the implementation.</p>
     *
     * @return randomized data
     */
    V randomizeData();
}
