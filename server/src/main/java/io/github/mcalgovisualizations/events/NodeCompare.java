package io.github.mcalgovisualizations.events;

import io.github.mcalgovisualizations.algorithms.TreeSearch.Node;
import io.github.mcalgovisualizations.visualization.algorithm.IAlgorithmEvent;
import org.jetbrains.annotations.NotNull;

public record NodeCompare<V extends Comparable<V>>(Node x, Node y) implements IAlgorithmEvent {
    @Override
    public @NotNull String toString() {
        return "Compare(" + x.value() + " , " + y.value() + ")";
    }
}
