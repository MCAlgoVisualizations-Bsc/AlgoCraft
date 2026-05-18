package io.github.mcalgovisualizations.prefab.algorithms.context;

import io.github.mcalgovisualizations.visualization.models.AbstractContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SortingContext<T extends Comparable<T>> extends AbstractContext<List<T>> {
    public SortingContext(List<T> values) {
        super(values);
    }

    @Override
    public AbstractContext<List<T>> copy() {
        return new SortingContext<>(copyData());
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

    public void swap(int idx1, int idx2) {
        var tmp = values.get(idx1);
        values.set(idx1, values.get(idx2));
        values.set(idx2, tmp);
    }
}
