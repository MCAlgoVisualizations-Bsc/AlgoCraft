package io.github.mcalgovisualizations.visualization.algorithms;

import io.github.mcalgovisualizations.visualization.flow.FlowNetworkRules;
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
        if (isFlowMatrix(initialData)) {
            this.initialData = List.copyOf(randomizedFlowMatrix(seed));
            return build();
        }

        var shuffled = new ArrayList<>(initialData);
        Collections.shuffle(shuffled, new Random(seed));
        this.initialData = List.copyOf(shuffled);
        return build();
    }

    private static <T extends Comparable<T>> boolean isFlowMatrix(List<Data<T>> data) {
        if (data.size() != FlowNetworkRules.MATRIX_SIZE) {
            return false;
        }
        for (Data<T> cell : data) {
            if (!(cell.value() instanceof Integer)) {
                return false;
            }
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    private List<Data<T>> randomizedFlowMatrix(int seed) {
        Random random = new Random(seed);
        int nodeCount = FlowNetworkRules.NODE_COUNT;
        int[] capacities = new int[nodeCount * nodeCount];

        for (int from = 0; from < nodeCount; from++) {
            for (int to = 0; to < nodeCount; to++) {
                int index = (from * nodeCount) + to;
                if (!FlowNetworkRules.isAllowedDirectedEdge(from, to)) {
                    capacities[index] = 0;
                    continue;
                }

                capacities[index] = random.nextDouble() > 0.4
                        ? random.nextInt(20) + 1
                        : 0;
            }
        }

        forceSpine(capacities, random, nodeCount);

        List<Data<T>> randomized = new ArrayList<>(capacities.length);
        for (int capacity : capacities) {
            randomized.add((Data<T>) new Data<>(capacity));
        }
        return randomized;
    }

    private static void forceSpine(int[] capacities, Random random, int nodeCount) {
        int[] guaranteedPath = {0, 1, 3, 5};
        for (int i = 0; i < guaranteedPath.length - 1; i++) {
            int from = guaranteedPath[i];
            int to = guaranteedPath[i + 1];
            int index = (from * nodeCount) + to;

            if (capacities[index] <= 0) {
                capacities[index] = random.nextInt(15) + 5;
            }
        }
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