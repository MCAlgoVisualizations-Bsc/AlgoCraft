package io.github.mcalgovisualizations.algorithms.context;

import io.github.mcalgovisualizations.visualization.algorithm.IAlgorithmEvent;
import io.github.mcalgovisualizations.visualization.models.AbstractContext;

import java.util.List;

public class GridContext<T> extends AbstractContext<List<T>> {
    public GridContext(List<T> values) {
        super(values);
    }
}
