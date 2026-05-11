package io.github.mcalgovisualizations.visualization.algorithm;

/**
 * Defines an algorithm that produces a sequence of {@link IAlgorithmEvent}
 * based on a provided context.
 *
 * <p>Implementations are expected to execute a sorting (or sorting-like)
 * procedure while emitting events that describe each significant step
 * (e.g. comparisons, swaps).</p>
 *
 * <p>The provided context acts as both the data source and the
 * event sink.</p>
 *
 * @param <C> the type of context used during execution
 */
public interface IPlayerSort<C> {

    /**
     * Executes the algorithm using the given context.
     *
     * @param context the execution context containing input data and/or
     *                mechanisms for emitting {@link IAlgorithmEvent}s
     *
     * Implementations should:
     * <ul>
     *     <li>Be deterministic for the same input</li>
     *     <li>Emit events in a consistent, ordered manner</li>
     *     <li>Avoid mutating external state outside the provided context</li>
     * </ul>
     */
    void run(C context);
}
