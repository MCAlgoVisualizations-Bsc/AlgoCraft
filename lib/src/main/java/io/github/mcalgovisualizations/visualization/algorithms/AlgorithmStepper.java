package io.github.mcalgovisualizations.visualization.algorithms;

import io.github.mcalgovisualizations.visualization.algorithms.events.Complete;
import io.github.mcalgovisualizations.visualization.algorithms.events.IAlgorithmEvent;
import io.github.mcalgovisualizations.visualization.algorithms.events.NoOp;
import io.github.mcalgovisualizations.visualization.models.Data;
import io.github.mcalgovisualizations.visualization.models.SortingCollection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class AlgorithmStepper<T extends Comparable<T>> {
    private final Random RANDOM = new Random(64);
    private final ArrayList<IAlgorithmEvent> history = new ArrayList<>();
    private SortingCollection<T> collection; // TODO : Make an interface for this
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
    public List<Data<T>> getBackingCollection() {
        var out = List.copyOf(collection.data());
        algorithm.sort(collection);
        var parsedEvents = new ArrayList<>(collection.events());
        parsedEvents.add(new Complete(collection.size()));

        this.history.addAll(parsedEvents);
        this.historyPointer = 0;

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
        return history.get(historyPointer--);
    }

    public List<Data<T>> randomizeCollection(int seed) {
        RANDOM.setSeed(seed);
        // create new collection with randomized data
        var data = new ArrayList<>(collection.data());
        Collections.shuffle(data, RANDOM);
        this.collection.clear();
        this.collection = new SortingCollection<>(data);

        // clear history and start over
        this.history.clear();
        this.historyPointer = 0;
        this.algorithm.sort(collection);
        var parsedEvents = new ArrayList<>(collection.events());
        parsedEvents.add(new Complete(collection.size()));
        this.history.addAll(parsedEvents);

        return List.copyOf(data);
    }
}
