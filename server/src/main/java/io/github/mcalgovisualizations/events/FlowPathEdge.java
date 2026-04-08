package io.github.mcalgovisualizations.events;

import io.github.mcalgovisualizations.visualization.algorithms.IAlgorithmEvent;
import org.jetbrains.annotations.NotNull;

public record FlowPathEdge(int from, int to, int slot, int pathFlow) implements IAlgorithmEvent {
    @Override
    public @NotNull String toString() {
        return "FlowPathEdge(" + from + "->" + to + ", slot=" + slot + ", pathFlow=" + pathFlow + ")";
    }
}

