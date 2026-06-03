package io.github.mcalgovisualizations.visualization.algorithm;

/**
 * Represents an algorithm that emits a deterministic sequence of playback events.
 *
 * <p>The algorithm reads and mutates the provided context while emitting
 * {@link IAlgorithmEvent}s that describe each meaningful step of execution.</p>
 *
 * @param <C> the type of context used during execution
 */
public interface IPlayerSort<C> {

    /**
     * Executes the algorithm using the given context.
     *
     * @param context the execution context containing input data and event emission support
     */
    void run(C context);
}
