package io.github.mcalgovisualizations.algorithms;

import io.github.mcalgovisualizations.visualization.algorithms.IPlayerSort;
import io.github.mcalgovisualizations.visualization.algorithms.events.Compare;
import io.github.mcalgovisualizations.visualization.models.ISort;

import java.util.ArrayList;
import java.util.List;


public final class PlayerUnorderedTree implements IPlayerSort {


    @Override
    public <T extends Comparable<T>> void sort(ISort<T> values) {
        int size = values.size();
        List<T> data = readIntInput(values);

        int targetIndex = size - 1; // Let's search for the last element
        T targetValue = data.get(targetIndex);

        // In an unordered tree, we just check every index from 0 to N
        for (int i = 0; i < size; i++) {
            T currentVal = data.get(i);

            // Emit the comparison for EVERY node until we find it
            values.emit(new Compare(i, targetIndex, currentVal, targetValue));

            if (currentVal == targetValue) {
                return; // Found by brute force
            }
        }
    }

    private static <T extends Comparable<T>> List<T> readIntInput(ISort<T> values) {
        List<T> out = new ArrayList<>(values.size());
        for (int i = 0; i < values.size(); i++) {
            T raw = values.get(i);
            out.add(i, raw);
        }
        return out;
    }
}