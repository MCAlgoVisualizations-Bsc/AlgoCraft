package io.github.mcalgovisualizations.algorithms;

import io.github.mcalgovisualizations.events.Compare;
import io.github.mcalgovisualizations.events.Swap;
import io.github.mcalgovisualizations.visualization.algorithms.IPlayerSort;
import io.github.mcalgovisualizations.visualization.models.ISort;

public class PlayerInsertion implements IPlayerSort {

    @Override
    public <T extends Comparable<T>> void sort(ISort<T> values) {
        int n = values.size();
        for (int i = 1; i < n; i++) {
            int j = i;

            var x = values.get(j);
            var y = values.get(j - 1);


            values.emit(new Compare(j, j-1, y, x));
            while (j > 0 && x.compareTo(y) < 0) {
                values.emit(new Swap(j, j - 1, y, x));
                values.swap(j, j - 1);
                j--;
            }
        }
    }
}