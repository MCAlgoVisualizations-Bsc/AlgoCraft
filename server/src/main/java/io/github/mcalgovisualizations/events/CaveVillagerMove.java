package io.github.mcalgovisualizations.events;

import io.github.mcalgovisualizations.visualization.algorithm.IAlgorithmEvent;
import org.jetbrains.annotations.NotNull;
@Deprecated
public record CaveVillagerMove(int slot, int algorithmSpeed) implements IAlgorithmEvent {
    @Override
    public @NotNull String toString() {
        return "CaveVillagerMove(slot=" + slot + ", algorithmSpeed=" + algorithmSpeed + ")";
    }
}

