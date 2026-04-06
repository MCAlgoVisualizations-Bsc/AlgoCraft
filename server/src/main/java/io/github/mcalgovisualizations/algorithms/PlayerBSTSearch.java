package io.github.mcalgovisualizations.algorithms;

import io.github.mcalgovisualizations.visualization.algorithms.IPlayerSort;
import io.github.mcalgovisualizations.visualization.algorithms.events.Compare;
import io.github.mcalgovisualizations.visualization.models.ISort;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class PlayerBSTSearch implements IPlayerSort {

    @Override
    public <T extends Comparable<T>> void sort(ISort<T> values) {
        int size = values.size();
        if (size == 0) {
            return;
        }

        List<T> data = readIntInput(values);
        if (data.isEmpty()) {
            return;
        }

        int[] left = new int[size];
        int[] right = new int[size];
        Arrays.fill(left, -1);
        Arrays.fill(right, -1);

        int root = 0;
        for (int insert = 1; insert < size; insert++) {
            T candidate = data.get(insert);
            int cursor = root;
            while (true) {
                if (candidate.compareTo(data.get(cursor)) < 0) {
                    if (left[cursor] == -1) {
                        left[cursor] = insert;
                        break;
                    }
                    cursor = left[cursor];
                } else {
                    if (right[cursor] == -1) {
                        right[cursor] = insert;
                        break;
                    }
                    cursor = right[cursor];
                }
            }
        }

        int targetIndex = size / 2;
        T target = data.get(targetIndex);

        int cursor = root;
        while (cursor != -1) {
            T current = data.get(cursor);
            values.emit(new Compare(cursor, targetIndex, current, target));

            if (current == target) {
                return;
            }

            int cmp = target.compareTo(current);
            if (cmp < 0) {
                cursor = left[cursor];
            } else {
                cursor = right[cursor];
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

