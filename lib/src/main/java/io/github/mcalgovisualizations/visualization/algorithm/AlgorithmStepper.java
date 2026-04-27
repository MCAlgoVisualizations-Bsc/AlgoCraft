package io.github.mcalgovisualizations.visualization.algorithm;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Iterates over a fixed sequence of {@link IAlgorithmEvent} with forward and backward navigation.
 *
 * <p>The stepper maintains an internal pointer representing the current position in the history.
 * Calling {@link #step()} returns the current event and advances the pointer.
 * Calling {@link #back()} moves the pointer backwards and returns the previous event.</p>
 *
 * <p>This class is immutable with respect to the underlying history. The provided list is defensively copied.</p>
 */
public final class AlgorithmStepper {

    private final List<IAlgorithmEvent> history;
    private int historyPointer = 0;

    /**
     * Creates a new stepper over the given event history.
     *
     * @param history the ordered list of events to iterate through
     * @throws NullPointerException if {@code history} is null
     */
    public AlgorithmStepper(@NotNull List<IAlgorithmEvent> history) {
        this.history = List.copyOf(history);
    }

    /**
     * Returns the current event and advances the pointer.
     *
     * @return the next event, or {@code null} if all events have been consumed
     */
    public @Nullable IAlgorithmEvent step() {
        if (isComplete()) return null;
        return history.get(historyPointer++);
    }

    /**
     * Moves the pointer backwards and returns the previous event.
     *
     * @return the previous event, or {@code null} if already at the beginning
     */
    public @Nullable IAlgorithmEvent back() {
        if (isAtBeginning()) return null;
        return history.get(--historyPointer);
    }

    /**
     * @return {@code true} if all events have been stepped through
     */
    public boolean isComplete() {
        return historyPointer >= history.size();
    }

    /**
     * @return {@code true} if the pointer is at the start of the history
     */
    public boolean isAtBeginning() {
        return historyPointer <= 0;
    }

    /**
     * @return the total number of events in the history
     */
    public int getHistorySize() {
        return history.size();
    }

    /**
     * Resets the pointer to the beginning of the history.
     */
    public void reset() {
        historyPointer = 0;
    }
}