package io.github.mcalgovisualizations.events;

import io.github.mcalgovisualizations.visualization.algorithm.IAlgorithmEvent;
import org.jetbrains.annotations.NotNull;
@Deprecated
public record FlowEdgeVisit(char from, char to, int slot, int residualCapacity) implements IAlgorithmEvent {
    @Override
    public @NotNull String toString() {
        return "FlowEdgeVisit(" + from + "->" + to + ", slot=" + slot + ", Max capacity=" + residualCapacity + ")";
    }
}

