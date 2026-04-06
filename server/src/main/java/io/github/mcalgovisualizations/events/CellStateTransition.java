package io.github.mcalgovisualizations.visualization.algorithms.events;

import org.jetbrains.annotations.NotNull;

public record CellStateTransition(int slot, CellState first, CellState second) implements IAlgorithmEvent {
    @Override
    public @NotNull String toString() {
        return "CellStateTransition(slot=" + slot + ", " + first + "<->" + second + ")";
    }
}

