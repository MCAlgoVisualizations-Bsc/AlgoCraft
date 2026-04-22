package io.github.mcalgovisualizations.visualization.renderer;

import net.minestom.server.coordinate.Pos;
import org.jetbrains.annotations.NotNull;

public record LayoutResult(@NotNull Object value, @NotNull Pos pos, IDisplayValue displayValue) {
    @Override
    public @NotNull String toString() {
        return "LayoutEntry{" + "pos=" + pos + ", idx=" + value + '}';
    }
}
