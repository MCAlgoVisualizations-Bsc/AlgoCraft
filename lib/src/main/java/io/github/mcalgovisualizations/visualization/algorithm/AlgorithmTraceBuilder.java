package io.github.mcalgovisualizations.visualization.algorithm;

import io.github.mcalgovisualizations.visualization.models.AlgorithmContext;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class AlgorithmTraceBuilder<T, C extends AlgorithmContext<T>> {
    private final IPlayerSort<C> algorithm;
    private T initialData;
    private final C context;

    public AlgorithmTraceBuilder(
            @NotNull IPlayerSort<C> algorithm,
            @NotNull C context
    ) {
        this.algorithm = algorithm;
        this.initialData = context.copyData();
        this.context = context;

    }


    /**
     * Builds a fresh immutable trace from the current initial data.
     */
    public AlgorithmTrace<T> build() {
        var initialSnapshot = context.copyData();
        var context = this.context.copy();

        algorithm.run(context.copy());

        return new AlgorithmTrace<>(
                initialSnapshot,
                context.copyData(),
                List.copyOf(context.getEvents())
        );
    }

    /**
     * Replaces the initial data with a shuffled version, then builds a new trace.
     */
    public @NotNull AlgorithmTrace<T> randomizeAndBuild() {
        this.initialData = context.randomizeData();
        return build();
    }

    /**
     * Returns the current initial data snapshot.
     */
    public @NotNull T getInitialData() {
        return context.copyData();
    }

    public record AlgorithmTrace<T>(
            T initialData,
            T finalData,
            List<IAlgorithmEvent> history
    ) {}
}