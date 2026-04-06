package io.github.mcalgovisualizations.visualization.algorithms;

import io.github.mcalgovisualizations.visualization.models.Data;
import io.github.mcalgovisualizations.visualization.models.ISort;
import io.github.mcalgovisualizations.visualization.models.SortingCollection;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public final class AlgorithmTraceBuilder<T extends Comparable<T>> {
    private final IPlayerSort algorithm;
    private List<Data<T>> initialData;

    public AlgorithmTraceBuilder(
            @NotNull IPlayerSort algorithm,
            @NotNull ISort<T> collection
    ) {
        this.algorithm = algorithm;
        this.initialData = List.copyOf(collection.data());
    }

    /**
     * Builds a fresh immutable trace from the current initial data.
     */
    public @NotNull AlgorithmTrace<T> build() {
        var workingCollection = new SortingCollection<>(initialData);

        algorithm.sort(workingCollection);

        return new AlgorithmTrace<>(
                List.copyOf(initialData),
                List.copyOf(workingCollection.data()),
                List.copyOf(workingCollection.events())
        );
    }

    /**
     * Replaces the initial data with a shuffled version, then builds a new trace.
     */
    public @NotNull AlgorithmTrace<T> randomizeAndBuild(int seed) {
        var shuffled = new ArrayList<>(initialData);
        Collections.shuffle(shuffled, new Random(seed));
        this.initialData = List.copyOf(shuffled);
        return build();
    }

    /**
     * Replaces the source data entirely.
     */
    public void setInitialData(@NotNull List<Data<T>> data) {
        this.initialData = List.copyOf(data);
    }

    /**
     * Returns the current initial data snapshot.
     */
    public @NotNull List<Data<T>> getInitialData() {
        return List.copyOf(initialData);
    }

    public record AlgorithmTrace<T extends Comparable<T>>(
            List<Data<T>> initialData,
            List<Data<T>> finalData,
            List<IAlgorithmEvent> history
    ) {}
}