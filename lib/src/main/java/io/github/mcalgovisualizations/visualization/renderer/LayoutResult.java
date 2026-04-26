package io.github.mcalgovisualizations.visualization.renderer;

import net.minestom.server.coordinate.Pos;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the result of placing a model value in the visual layout.
 *
 * <p>A {@code LayoutResult} connects a source value to a world position and,
 * optionally, an existing display element assigned to that position.</p>
 *
 * @param value the model value represented by this layout entry
 * @param pos the computed world position for the value
 * @param displayValue the display element associated with this value, or {@code null} if none has been assigned
 */
public record LayoutResult(
        @NotNull Object value,
        @NotNull Pos pos,
        IDisplayValue displayValue
) {
    /**
     * Returns a concise debug representation of this layout entry.
     *
     * @return debug string containing the position and value
     */
    @Override
    public @NotNull String toString() {
        return "LayoutEntry{" + "pos=" + pos + ", idx=" + value + '}';
    }
}