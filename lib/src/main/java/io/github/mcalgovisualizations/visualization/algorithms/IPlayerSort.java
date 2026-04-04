package io.github.mcalgovisualizations.visualization.algorithms;

import io.github.mcalgovisualizations.visualization.models.ISort;

public interface IPlayerSort {
    <T extends Comparable<T>> void sort(ISort<T> values);
    String getName();
}
