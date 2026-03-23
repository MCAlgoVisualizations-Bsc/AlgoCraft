package io.github.mcalgovisualizations.visualization.algorithms;

import io.github.mcalgovisualizations.visualization.algorithms.events.Complete;
import io.github.mcalgovisualizations.visualization.algorithms.events.IAlgorithmEvent;
import io.github.mcalgovisualizations.visualization.algorithms.events.NoOp;
import io.github.mcalgovisualizations.visualization.models.Data;
import io.github.mcalgovisualizations.visualization.models.SortingCollection;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class AlgorithmStepper<T extends Comparable<T>> {
    private final ArrayList<IAlgorithmEvent> history = new ArrayList<>();
    private SortingCollection<T> collection; // TODO : Make an interface for this
    private final IPlayerSort algorithm;

    private int historyPointer = 0;

    public AlgorithmStepper(@NotNull IPlayerSort algorithm, @NotNull SortingCollection<T> collection) {
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

    public IAlgorithmEvent step() {
        // check for empty history?
        if(history.isEmpty()) return new NoOp();
        // check if we are already at the end of the history
        if(historyPointer >= history.size()) return new NoOp();
        return history.get(historyPointer++);
    }

    public IAlgorithmEvent back() {
        // check for empty history?
        if(history.isEmpty()) return new NoOp();
        // check if we are already at the beginning of the history
        if (historyPointer <= 0) return new NoOp();
        return history.get(--historyPointer);
    }

    public List<Data<T>> randomizeCollection(int seed) {
        var data = new ArrayList<>(collection.data());
        Collections.shuffle(data, new Random(seed));

        this.collection.clear();
        this.collection = new SortingCollection<>(data);

        rebuildHistory();
        return List.copyOf(data);
    }

    private void rebuildHistory() {
        history.clear();
        historyPointer = 0;
        algorithm.sort(collection);
        history.addAll(collection.events());
        history.add(new Complete(collection.size()));
    }

    public String getAlgoName() {
        return algorithm.getName();
    }
}
