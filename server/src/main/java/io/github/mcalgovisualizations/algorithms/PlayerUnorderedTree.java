package io.github.mcalgovisualizations.algorithms;

import io.github.mcalgovisualizations.algorithms.context.GridContext;
import io.github.mcalgovisualizations.algorithms.context.SortingContext;
import io.github.mcalgovisualizations.visualization.algorithm.IPlayerSort;
import io.github.mcalgovisualizations.events.Compare;


public final class PlayerUnorderedTree<T extends Comparable<T>> implements IPlayerSort<GridContext<T>> {


    @Override
    public void run(GridContext<T> ctx) {
        var values = ctx.getData();
        int size = values.size();

        int targetIndex = size - 1; // Let's search for the last element
        T targetValue = values.get(targetIndex);

        // In an unordered tree, we just check every index from 0 to N
        for (int i = 0; i < size; i++) {
            T currentVal = values.get(i);

            // Emit the comparison for EVERY node until we find it
            ctx.emit(new Compare(i, targetIndex, currentVal, targetValue));

            if (currentVal == targetValue) {
                return; // Found by brute force
            }
        }
    }
}