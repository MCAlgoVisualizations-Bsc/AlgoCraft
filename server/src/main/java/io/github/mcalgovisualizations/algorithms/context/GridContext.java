package io.github.mcalgovisualizations.algorithms.context;

import io.github.mcalgovisualizations.visualization.algorithm.IAlgorithmEvent;
import io.github.mcalgovisualizations.visualization.models.AbstractContext;
import io.github.mcalgovisualizations.visualization.models.AlgorithmContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GridContext<T> extends AbstractContext<List<T>> {
    public GridContext(List<T> values) {
        super(values);
    }


    @Override
    public GridContext<T> copy() {
        return new GridContext<>(copyData());
    }

    @Override
    public List<T> copyData() {
        return new ArrayList<>(List.copyOf(values));
    }

    @Override
    public List<T> randomizeData() {
        Collections.shuffle(values);
        return copyData();
    }
}
