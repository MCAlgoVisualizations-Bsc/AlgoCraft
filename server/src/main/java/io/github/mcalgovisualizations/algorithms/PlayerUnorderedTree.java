package io.github.mcalgovisualizations.algorithms;

import io.github.mcalgovisualizations.algorithms.context.SortingContext;
import io.github.mcalgovisualizations.visualization.algorithm.IPlayerSort;
import io.github.mcalgovisualizations.events.Compare;


public final class PlayerUnorderedTree<I extends Comparable<I>> implements IPlayerSort<SortingContext<I>> {


    @Override
    public void run(SortingContext<I> ctx) {
        var values = ctx.getData();
        int size = values.size();

        int targetIndex = size - 1; // Let's search for the last element
        I targetValue = values.get(targetIndex);

        // In an unordered tree, we just check every index from 0 to N
        for (int i = 0; i < size; i++) {
            I currentVal = values.get(i);

            // Emit the comparison for EVERY node until we find it
            ctx.emit(new Compare(i, targetIndex, currentVal, targetValue));

            if (currentVal == targetValue) {
                return; // Found by brute force
            }
        }
    }
}