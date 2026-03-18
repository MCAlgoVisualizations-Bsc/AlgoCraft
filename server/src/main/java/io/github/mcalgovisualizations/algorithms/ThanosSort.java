package io.github.mcalgovisualizations.algorithms;

import io.github.mcalgovisualizations.visualization.algorithms.IPlayerSort;
import io.github.mcalgovisualizations.visualization.models.SortingCollection;

public class ThanosSort implements IPlayerSort {
    @Override
    public <T extends Comparable<T>> void sort(SortingCollection<T> values) {
        int n = values.size();
        for (int i = 1; i < n; i++) {
            int j = i;
            while (j > 0 && values.compare(j, j - 1) < 0) {
                values.swap(j, j - 1);
                j--;
            }
        }
    }
}
