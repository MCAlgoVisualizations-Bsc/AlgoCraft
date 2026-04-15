package io.github.mcalgovisualizations.visualization.renderer;

import io.github.mcalgovisualizations.visualization.ui.IStylingProfile;
import net.minestom.server.coordinate.Pos;
import org.jetbrains.annotations.NotNull;

public record LayoutResult(@NotNull Object value, @NotNull Pos pos, @NotNull IStylingProfile styling) {
    @Override
    public @NotNull String toString() {
        return "LayoutEntry{" + "pos=" + pos + ", idx=" + value + '}';
    }
    public @NotNull IDisplayValue getDisplayValue() {
        return styling.applyStyle(value().toString(), pos());
    }
}
