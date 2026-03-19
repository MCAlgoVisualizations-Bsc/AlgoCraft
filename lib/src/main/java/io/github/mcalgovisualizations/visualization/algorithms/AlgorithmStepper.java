package io.github.mcalgovisualizations.visualization.algorithms;

import io.github.mcalgovisualizations.visualization.algorithms.events.Complete;
import io.github.mcalgovisualizations.visualization.algorithms.events.IAlgorithmEvent;
import io.github.mcalgovisualizations.visualization.algorithms.events.NoOp;
import io.github.mcalgovisualizations.visualization.models.Data;
import io.github.mcalgovisualizations.visualization.models.SortingCollection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AlgorithmStepper<T extends Comparable<T>> {

    private final ArrayList<IAlgorithmEvent> history = new ArrayList<>();
    private final SortingCollection<T> collection; // TODO : Make an interface for this
    private final IPlayerSort algorithm;

    private int historyPointer = 0;

    public AlgorithmStepper(IPlayerSort algorithm, SortingCollection<T> collection) {
        this.algorithm = algorithm;
        this.collection = collection;
    }

    /**
     * Used to start the algorithm, sort the collection and return a copy of the backing collection.
     * @return a copy of the backing collection.
     */
    public List<Data<T>> onStart() {
        algorithm.sort(collection);
        var parsedEvents = new ArrayList<>(collection.events());
        parsedEvents.add(new Complete(collection.size()));

        this.history.addAll(parsedEvents);
        this.historyPointer = 0;

        return List.copyOf(collection.data());
    }

    public void onCleanup() {
        this.history.clear();
        this.collection.clear();
    }

    public IAlgorithmEvent step() {
        // check for empty history?
        if(history.isEmpty()) return new NoOp();
        // check if we are already at the end of the history
        if(historyPointer + 1 >= history.size()) return new NoOp();
        historyPointer++;
        return history.get(historyPointer);
    }

    public IAlgorithmEvent back() {
        // check for empty history?
        if(history.isEmpty()) return new NoOp();
        // check if we are already at the beginning of the history
        if ((historyPointer - 1) < 0) return new NoOp();
        this.historyPointer--;
        return history.get(historyPointer);
    }
}
