package io.github.mcalgovisualizations.visualization.renderer;

import io.github.mcalgovisualizations.visualization.layouts.IStylingProfile;
import io.github.mcalgovisualizations.visualization.models.Data;
import net.minestom.server.coordinate.Pos;
import org.jetbrains.annotations.NotNull;

public record LayoutResult<T extends Comparable<T>>(@NotNull Data<T> value, @NotNull Pos pos, @NotNull IStylingProfile styling) {
    @Override
    public @NotNull String toString() {
        return "LayoutEntry{" + "pos=" + pos + ", idx=" + value.value() + '}';
    }
    public @NotNull IDisplayValue getDisplayValue() {
        return styling.applyStyle(value().toString(), pos());
    }
}
