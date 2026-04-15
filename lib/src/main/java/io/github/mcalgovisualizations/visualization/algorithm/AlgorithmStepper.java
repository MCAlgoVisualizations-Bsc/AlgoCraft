package io.github.mcalgovisualizations.visualization.algorithm;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class AlgorithmStepper {
    private final List<IAlgorithmEvent> history;
    private int historyPointer = 0;

    public AlgorithmStepper(@NotNull List<IAlgorithmEvent> history) {
        this.history = List.copyOf(history);
    }

    public @Nullable IAlgorithmEvent step() {
        if (isComplete()) return null;
        return history.get(historyPointer++);
    }

    public @Nullable IAlgorithmEvent back() {
        if (isAtBeginning()) return null;
        return history.get(--historyPointer);
    }

    public boolean isComplete() {
        return historyPointer >= history.size();
    }

    public boolean isAtBeginning() {
        return historyPointer <= 0;
    }

    public int getHistorySize() {
        return history.size();
    }

    public void reset() {
        historyPointer = 0;
    }

}