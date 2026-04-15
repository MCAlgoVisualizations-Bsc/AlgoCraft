package io.github.mcalgovisualizations.events;

import io.github.mcalgovisualizations.visualization.algorithms.IAlgorithmEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Event emitted when the villager (representing algorithm's current head) moves to a new cell.
 */
public record VillagerMove(int slot) implements IAlgorithmEvent {
    @Override
    public @NotNull String toString() {
        return "VillagerMove(slot=" + slot + ")";
    }
}

