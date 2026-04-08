package io.github.mcalgovisualizations.events;

import io.github.mcalgovisualizations.visualization.algorithms.IAlgorithmEvent;
import org.jetbrains.annotations.NotNull;

public record FlowEdgeVisit(int from, int to, int slot, int residualCapacity) implements IAlgorithmEvent {
    @Override
    public @NotNull String toString() {
        return "FlowEdgeVisit(" + from + "->" + to + ", slot=" + slot + ", residual=" + residualCapacity + ")";
    }
}

