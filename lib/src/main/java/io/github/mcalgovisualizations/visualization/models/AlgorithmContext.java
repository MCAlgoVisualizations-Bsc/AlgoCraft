package io.github.mcalgovisualizations.visualization.models;

import io.github.mcalgovisualizations.visualization.algorithms.IAlgorithmEvent;

import java.util.List;

public interface AlgorithmContext<I> {
    I getData();
    List<IAlgorithmEvent> getEvents();
    void emit(IAlgorithmEvent e);
}
