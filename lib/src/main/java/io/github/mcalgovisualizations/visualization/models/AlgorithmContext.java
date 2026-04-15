package io.github.mcalgovisualizations.visualization.models;

import io.github.mcalgovisualizations.visualization.algorithm.IAlgorithmEvent;

import java.util.List;

public interface AlgorithmContext<V> {
    V getData();
    List<IAlgorithmEvent> getEvents();
    void emit(IAlgorithmEvent e);
    <C extends AlgorithmContext<V>> C copy();
    V copyData();
    V randomizeData();
}
