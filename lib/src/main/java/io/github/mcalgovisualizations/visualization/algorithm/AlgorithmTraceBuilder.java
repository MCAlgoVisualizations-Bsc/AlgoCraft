package io.github.mcalgovisualizations.visualization.algorithm;

import io.github.mcalgovisualizations.visualization.models.AlgorithmContext;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Builds immutable playback traces by running an algorithm against a copied context.
 *
 * <p>The builder captures the initial model, executes the algorithm once, and stores
 * the resulting event history so playback can be reproduced later.</p>
 *
 * @param <T> model value type
 * @param <C> context type
 */
public final class AlgorithmTraceBuilder<T, C extends AlgorithmContext<T>> {
    private final IPlayerSort<C> algorithm;
    private T initialData;
    private final C context;

    /**
     * Creates a new trace builder for the given algorithm and context.
     *
     * @param algorithm algorithm implementation that emits events
     * @param context source context used to build traces
     */
    public AlgorithmTraceBuilder(
            @NotNull IPlayerSort<C> algorithm,
            @NotNull C context
    ) {
        this.algorithm = algorithm;
        this.initialData = context.copyData();
        this.context = context;
    }

    /**
     * Runs the algorithm once and captures the resulting trace.
     *
     * <p>The algorithm is executed on a copied context so the original input remains
     * untouched.</p>
     *
     * @return a fresh immutable algorithm trace
     */
    public AlgorithmTrace<T> build() {
        var initialSnapshot = context.copyData();
        var context = this.context.copy();

        // TODO: remove the casting? More type safety!!
        algorithm.run((C) context);

        return new AlgorithmTrace<>(
                initialSnapshot,
                context.copyData(),
                List.copyOf(context.getEvents())
        );
    }

    /**
     * Randomizes the source data and then captures a new trace.
     *
     * @return a fresh immutable randomized algorithm trace
     */
    public @NotNull AlgorithmTrace<T> randomizeAndBuild() {
        this.initialData = context.randomizeData();
        return build();
    }

    /**
     * Returns the initial data snapshot used by the builder.
     *
     * @return the current initial data
     */
    public @NotNull T getInitialData() {
        return context.copyData();
    }

    /**
     * Immutable algorithm trace data.
     *
     * <p>The trace contains the starting model, the final model, and the ordered
     * history of events emitted during execution.</p>
     *
     * @param initialData the starting model snapshot
     * @param finalData the model state after the algorithm finishes
     * @param history the ordered event history
     */
    public record AlgorithmTrace<T>(
            T initialData,
            T finalData,
            List<IAlgorithmEvent> history
    ) {}
}