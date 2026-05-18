package io.github.mcalgovisualizations.prefab.events;

import io.github.mcalgovisualizations.visualization.algorithm.IAlgorithmEvent;
import org.jetbrains.annotations.NotNull;
@Deprecated
public record FlowStatus(Type type, int value, int total) implements IAlgorithmEvent {

    public enum Type {
        INVALID_INPUT,
        AUGMENTED,
        COMPLETE
    }

    @Override
    public @NotNull String toString() {
        return "FlowStatus(type=" + type + ", value=" + value + ", total=" + total + ")";
    }
}

