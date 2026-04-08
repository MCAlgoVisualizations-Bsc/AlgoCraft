package io.github.mcalgovisualizations.algorithms.context;

import io.github.mcalgovisualizations.visualization.algorithms.IAlgorithmEvent;
import io.github.mcalgovisualizations.visualization.models.AbstractContext;

import java.util.List;

public class GridContext extends AbstractContext<List<Integer>> {
    public GridContext(List<Integer> values, List<IAlgorithmEvent> events) {
        super(values, events);
    }
}
