package io.github.mcalgovisualizations.visualization.models;

import io.github.mcalgovisualizations.visualization.algorithms.events.Compare;
import io.github.mcalgovisualizations.visualization.algorithms.events.IAlgorithmEvent;
import io.github.mcalgovisualizations.visualization.algorithms.events.Swap;
import org.jetbrains.annotations.NotNull;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SortingCollection<T extends Comparable<T>> extends AbstractCollection<T> implements ISort<T> {


    private final List<Data<T>> data;
    private final List<IAlgorithmEvent> events = new ArrayList<>();

    public SortingCollection(List<Data<T>> data) {
        this.data = new ArrayList<>(data);
    }

    public List<IAlgorithmEvent> events() {
        return List.copyOf(events);
    }

    @Override
    public boolean add(T t) {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull Iterator<T> iterator() {
        return data.stream().map(Data::value).iterator();
    }

    @Override
    public int size() {
        return data.size();
    }

    @Override
    public void swap(int i, int j) {
        if (i == j) return;
        events.add(new Swap(i, j, data.get(i), data.get(j)));
        Data<T> temp = data.get(i);
        data.set(i, data.get(j));
        data.set(j, temp);
    }

    @Override
    public int compare(int i, int j) {
        events.add(new Compare(i, j, data.get(i), data.get(j)));
        var s = "sad";
        return data.get(i).value().compareTo(data.get(j).value());
    }

    @Override
    public T get(int idx) {
        return data.get(idx).value();
    }

    public List<Data<T>> data() {
        return List.copyOf(data);
    }

    public void clear() {
        this.data.clear();
        this.events.clear();
    }
}