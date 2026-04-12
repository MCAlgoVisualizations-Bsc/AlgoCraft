package io.github.mcalgovisualizations.visualization.algorithm;

import io.github.mcalgovisualizations.visualization.models.AlgorithmContext;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class AlgorithmTraceBuilder<T, C extends AlgorithmContext<T>> {
    private final IPlayerSort<C> algorithm;
    private T initialData;
    private final ContextFactory<T, C> factory;

    public AlgorithmTraceBuilder(
            @NotNull IPlayerSort<C> algorithm,
            @NotNull T initialData,
            @NotNull ContextFactory<T, C> contextFactory
    ) {
        this.algorithm = algorithm;
        this.initialData = contextFactory.copy(initialData);
        this.factory = contextFactory;
    }

    /**
     * Builds a fresh immutable trace from the current initial data.
     */
    public AlgorithmTrace<T> build() {
        var initialSnapshot = factory.copy(initialData);
        var context = factory.create(initialData);

        algorithm.run(context);

        return new AlgorithmTrace<>(
                initialSnapshot,
                factory.copy(context.getData()),
                List.copyOf(context.getEvents())
        );
    }

    /**
     * Replaces the initial data with a shuffled version, then builds a new trace.
     */
    public @NotNull AlgorithmTrace<T> randomizeAndBuild() {
        this.initialData = factory.randomize(initialData);
        return build();
    }

    /**
     * Replaces the source data entirely.
     */
    public void setInitialData(@NotNull T data) {
        this.initialData = factory.copy(data);
    }

    /**
     * Returns the current initial data snapshot.
     */
    public @NotNull T getInitialData() {
        return factory.copy(initialData);
    }

    public record AlgorithmTrace<T>(
            T initialData,
            T finalData,
            List<IAlgorithmEvent> history
    ) {}
}