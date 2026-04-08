package io.github.mcalgovisualizations.visualization.models;

import org.jetbrains.annotations.NotNull;

@Deprecated
public record Data<T extends Comparable<T>>(T value) {
    @Override
    public @NotNull String toString() {
        return value.toString();
    }
}
