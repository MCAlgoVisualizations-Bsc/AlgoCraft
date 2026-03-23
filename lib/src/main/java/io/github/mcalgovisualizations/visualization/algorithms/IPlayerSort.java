package io.github.mcalgovisualizations.visualization.algorithms;

import io.github.mcalgovisualizations.visualization.models.SortingCollection;

public interface IPlayerSort {
    <T extends Comparable<T>> void sort(SortingCollection<T> values);
    String getName();
}
