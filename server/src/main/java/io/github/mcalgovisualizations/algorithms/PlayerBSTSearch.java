package io.github.mcalgovisualizations.algorithms;

import io.github.mcalgovisualizations.visualization.algorithms.IPlayerSort;
import io.github.mcalgovisualizations.visualization.algorithms.events.Compare;
import io.github.mcalgovisualizations.visualization.models.ISort;

import java.util.Arrays;

public final class PlayerBSTSearch implements IPlayerSort {

    @Override
    public <T extends Comparable<T>> void sort(ISort<T> values) {
        int size = values.size();
        if (size == 0) {
            return;
        }

        int[] data = readIntInput(values);
        if (data.length == 0) {
            return;
        }

        int[] left = new int[size];
        int[] right = new int[size];
        Arrays.fill(left, -1);
        Arrays.fill(right, -1);

        int root = 0;
        for (int insert = 1; insert < size; insert++) {
            int candidate = data[insert];
            int cursor = root;
            while (true) {
                if (candidate < data[cursor]) {
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
        int target = data[targetIndex];

        int cursor = root;
        while (cursor != -1) {
            int current = data[cursor];
            values.emit(new Compare(cursor, targetIndex, current, target));

            if (current == target) {
                return;
            }

            if (target < current) {
                cursor = left[cursor];
            } else {
                cursor = right[cursor];
            }
        }
    }

    @Override
    public String getName() {
        return "BST Search";
    }

    private static <T extends Comparable<T>> int[] readIntInput(ISort<T> values) {
        int[] out = new int[values.size()];
        for (int i = 0; i < values.size(); i++) {
            T raw = values.get(i);
            if (!(raw instanceof Integer number)) {
                return new int[0];
            }
            out[i] = number;
        }
        return out;
    }
}

