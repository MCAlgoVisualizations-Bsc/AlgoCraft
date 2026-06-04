package io.github.mcalgovisualizations.prefab.events;

import io.github.mcalgovisualizations.visualization.algorithm.IAlgorithmEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Event emitted when a villager/pathfinding visual should move to a new slot.
 *
 * @param slot the target slot
 * @param algorithmSpeed the speed value associated with the movement
 */
public record VillagerMove(int slot, int algorithmSpeed) implements IAlgorithmEvent {
    @Override
    public @NotNull String toString() {
        return "VillagerMove(slot=" + slot + ", algorithmSpeed=" + algorithmSpeed + ")";
    }
}

