package io.github.mcalgovisualizations.prefab.events;

import io.github.mcalgovisualizations.visualization.algorithm.IAlgorithmEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Event emitted when two values are swapped.
 *
 * @param x first slot index
 * @param y second slot index
 * @param xValue value from the first slot before the swap
 * @param yValue value from the second slot before the swap
 */
public record Swap(int x, int y, Object xValue, Object yValue) implements IAlgorithmEvent {
    @Override
    public @NotNull String toString() {
        return "Swap("+ x + ": " + xValue + " , " + y + ": " + yValue + ")";
    }
}
