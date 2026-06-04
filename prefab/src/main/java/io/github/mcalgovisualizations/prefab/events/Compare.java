package io.github.mcalgovisualizations.prefab.events;

import io.github.mcalgovisualizations.visualization.algorithm.IAlgorithmEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Event emitted when two values are compared.
 *
 * @param x first slot index
 * @param y second slot index
 * @param xValue current value in the first slot
 * @param yValue current value in the second slot
 */
public record Compare(int x, int y, Object xValue, Object yValue) implements IAlgorithmEvent {
    @Override
    public @NotNull String toString() {
        return "Compare("+ x + ": " + xValue + " , " + y + ": " + yValue + ")";
    }
}
