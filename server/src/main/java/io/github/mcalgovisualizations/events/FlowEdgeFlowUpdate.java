package io.github.mcalgovisualizations.events;

import io.github.mcalgovisualizations.visualization.algorithm.IAlgorithmEvent;
import org.jetbrains.annotations.NotNull;

public record FlowEdgeFlowUpdate(int slot, int currentFlow) implements IAlgorithmEvent {
    @Override
    public @NotNull String toString() {
        return "FlowEdgeFlowUpdate(slot=" + slot + ", flow=" + currentFlow + ")";
    }
}

