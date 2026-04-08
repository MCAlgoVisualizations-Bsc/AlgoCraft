package io.github.mcalgovisualizations.visualization.models;

import io.github.mcalgovisualizations.visualization.algorithms.IAlgorithmEvent;

import java.util.List;

public interface ISort<T extends Comparable<T>> {
    void swap(int i, int j);
    int compare(int i, int j);
    T get(int i);
    ISort<T> copy();
    List<Data<T>> data();
    void clear();
    List<IAlgorithmEvent> events();
    void emit(IAlgorithmEvent event);
    int size();
}
