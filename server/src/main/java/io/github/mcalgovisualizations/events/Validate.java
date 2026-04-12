package io.github.mcalgovisualizations.events;

import io.github.mcalgovisualizations.visualization.algorithm.IAlgorithmEvent;
import org.jetbrains.annotations.NotNull;

public record Validate() implements IAlgorithmEvent {
    @Override
    public @NotNull String toString() {
        return "Validate()";
    }
}