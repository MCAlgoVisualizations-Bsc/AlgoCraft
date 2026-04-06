package io.github.mcalgovisualizations.visualization.algorithms.events;

import org.jetbrains.annotations.NotNull;

public record Swap(int x, int y, Object xValue, Object yValue) implements IAlgorithmEvent {
    @Override
    public @NotNull String toString() {
        return "Swap("+ x + ": " + xValue + " , " + y + ": " + yValue + ")";
    }
}
