package io.github.mcalgovisualizations.visualization.algorithms;

import io.github.mcalgovisualizations.visualization.algorithms.events.IAlgorithmEvent;
import io.github.mcalgovisualizations.visualization.models.Data;
import io.github.mcalgovisualizations.visualization.models.ISort;
import io.github.mcalgovisualizations.visualization.models.SortingCollection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class AlgorithmStepper<T extends Comparable<T>> {
    private final ArrayList<IAlgorithmEvent> history = new ArrayList<>();
    private ISort<T> collection; // TODO : Make an interface for this
    private final IPlayerSort algorithm;

    private int historyPointer = 0;

    public AlgorithmStepper(@NotNull IPlayerSort algorithm, @NotNull ISort<T> collection) {
        this.algorithm = algorithm;
        this.collection = collection;
    }

    /**
     * Used to start the algorithm, sort the collection and return a copy of the backing collection.
     * @return a copy of the backing collection.
     */
    public List<Data<T>> getBackingCollection() {
        var out = List.copyOf(collection.data());
        rebuildHistory();
        return out;
    }

    public void onCleanup() {
        this.history.clear();
        this.collection.clear();
    }

    public @Nullable IAlgorithmEvent step() {
        assertNotEmpty();
        if(isComplete()) return null;
        return history.get(historyPointer++);
    }

    public @Nullable IAlgorithmEvent back() {
        assertNotEmpty();
        if (isAtBeginning()) return null;
        return history.get(--historyPointer);
    }

    public List<Data<T>> randomizeCollection(int seed) {
        var data = new ArrayList<>(collection.data());

        // This is a hack, but it works for now
        // TODO : should be fixed in the collection
        Collections.shuffle(data, new Random(seed));

        this.collection.clear();
        this.collection = new SortingCollection<>(data);

        rebuildHistory();
        return List.copyOf(data);
    }

    public Boolean isComplete() {
        return historyPointer >= history.size();
    }

    public Boolean isAtBeginning() {
        return historyPointer <= 0;
    }

    private void rebuildHistory() {
        history.clear();
        historyPointer = 0;
        algorithm.sort(collection);
        history.addAll(collection.events());
    }

    public int getHistorySize() {
        return history.size();
    }

    public String getAlgoName() {
        return algorithm.getName();
    }

    private void assertNotEmpty() {
        if(history.isEmpty()) throw new IllegalStateException("Cannot step from empty history");
    }

}
