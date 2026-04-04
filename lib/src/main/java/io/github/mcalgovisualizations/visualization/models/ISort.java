package io.github.mcalgovisualizations.visualization.models;

import io.github.mcalgovisualizations.visualization.algorithms.events.IAlgorithmEvent;

import java.util.List;

public interface ISort<T extends Comparable<T>> {
    void swap(int i, int j);
    int compare(int i, int j);
    T get(int i);
    SortingCollection<T> copy();
    List<Data<T>> data();
    void clear();
    List<IAlgorithmEvent> events();
    void emit(IAlgorithmEvent event);
    int size();
}
