package io.github.mcalgovisualizations.algorithms.context;

import io.github.mcalgovisualizations.visualization.algorithm.IAlgorithmEvent;
import io.github.mcalgovisualizations.visualization.models.AbstractContext;

import java.util.List;

public class SortingContext<T extends Comparable<T>> extends AbstractContext<List<T>> {
    public SortingContext(List<T> values) {
        super(values);
    }

    public void swap(int idx1, int idx2) {
        var tmp =  values.get(idx1);
        values.set(idx1, values.get(idx2));
        values.set(idx2, tmp);
    }
}
