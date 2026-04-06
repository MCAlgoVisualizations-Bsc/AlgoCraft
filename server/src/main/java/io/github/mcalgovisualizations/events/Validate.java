package io.github.mcalgovisualizations.visualization.algorithms.events;

import org.jetbrains.annotations.NotNull;

public record Validate() implements IAlgorithmEvent {
    @Override
    public @NotNull String toString() {
        return "Validate()";
    }
}