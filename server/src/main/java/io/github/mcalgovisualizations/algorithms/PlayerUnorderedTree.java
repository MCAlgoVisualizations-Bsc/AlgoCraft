package io.github.mcalgovisualizations.algorithms;

import io.github.mcalgovisualizations.visualization.algorithms.IPlayerSort;
import io.github.mcalgovisualizations.visualization.algorithms.events.Compare;
import io.github.mcalgovisualizations.visualization.models.ISort;


public final class PlayerUnorderedTree implements IPlayerSort {


    @Override
    public <T extends Comparable<T>> void sort(ISort<T> values) {
        int size = values.size();
        int[] data = readIntInput(values);

        int targetIndex = size - 1; // Let's search for the last element
        int targetValue = data[targetIndex];

        // In an unordered tree, we just check every index from 0 to N
        for (int i = 0; i < size; i++) {
            int currentVal = data[i];

            // Emit the comparison for EVERY node until we find it
            values.emit(new Compare(i, targetIndex, currentVal, targetValue));

            if (currentVal == targetValue) {
                return; // Found by brute force
            }
        }
    }

    @Override
    public String getName() {
        return "Red-Black Tree Search";
    }

    private static <T extends Comparable<T>> int[] readIntInput(ISort<T> values) {
        int[] out = new int[values.size()];
        for (int i = 0; i < values.size(); i++) {
            T raw = values.get(i);
            if (!(raw instanceof Integer number)) return new int[0];
            out[i] = number;
        }
        return out;
    }
}