package io.github.mcalgovisualizations.events;

import io.github.mcalgovisualizations.visualization.algorithm.IAlgorithmEvent;
import org.jetbrains.annotations.NotNull;

public record VillagerMove(int slot, int algorithmSpeed) implements IAlgorithmEvent {
    @Override
    public @NotNull String toString() {
        return "VillagerMove(slot=" + slot + ", algorithmSpeed=" + algorithmSpeed + ")";
    }
}

