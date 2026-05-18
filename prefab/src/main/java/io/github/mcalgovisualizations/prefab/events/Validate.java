package io.github.mcalgovisualizations.prefab.events;

import io.github.mcalgovisualizations.visualization.algorithm.IAlgorithmEvent;
import org.jetbrains.annotations.NotNull;
@Deprecated
public record Validate() implements IAlgorithmEvent {
    @Override
    public @NotNull String toString() {
        return "Validate()";
    }
}