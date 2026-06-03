package io.github.mcalgovisualizations.prefab.events;

import io.github.mcalgovisualizations.visualization.algorithm.IAlgorithmEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Event emitted when a pathfinding cell changes between two states.
 *
 * @param slot the affected cell slot
 * @param first the previous state
 * @param second the new state
 */
public record CellStateTransition(int slot, CellState first, CellState second) implements IAlgorithmEvent {
    @Override
    public @NotNull String toString() {
        return "CellStateTransition(slot=" + slot + ", " + first + "<->" + second + ")";
    }
}

