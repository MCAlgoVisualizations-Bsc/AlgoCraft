package io.github.mcalgovisualizations.algorithms;

import io.github.mcalgovisualizations.visualization.algorithms.IPlayerSort;
import io.github.mcalgovisualizations.events.Compare;
import io.github.mcalgovisualizations.visualization.models.ISort;

import java.util.Arrays;

public final class PlayerTSTSearch implements IPlayerSort {

    @Override
    public <T extends Comparable<T>> void sort(ISort<T> values) {
        int size = values.size();
        if (size == 0) {
            return;
        }

        String[] data = readStringInput(values);
        if (data.length == 0) {
            return;
        }

        int[] left = new int[size];
        int[] middle = new int[size];
        int[] right = new int[size];
        Arrays.fill(left, -1);
        Arrays.fill(middle, -1);
        Arrays.fill(right, -1);

        int root = 0;
        for (int insert = 1; insert < size; insert++) {
            insert(root, insert, data, left, middle, right);
        }

        int targetIndex = size / 2;
        String target = data[targetIndex];

        int cursor = root;
        while (cursor != -1) {
            String current = data[cursor];
            values.emit(new Compare(cursor, targetIndex, current, target));

            int cmp = target.compareTo(current);
            if (cmp == 0) {
                return;
            }
            if (cmp < 0) {
                cursor = left[cursor];
            } else {
                cursor = right[cursor];
            }
        }
    }

    private static void insert(int root, int insert, String[] data, int[] left, int[] middle, int[] right) {
        int cursor = root;
        String candidate = data[insert];

        while (true) {
            String current = data[cursor];
            int cmp = candidate.compareTo(current);

            if (cmp < 0) {
                if (left[cursor] == -1) {
                    left[cursor] = insert;
                    return;
                }
                cursor = left[cursor];
            } else if (cmp > 0) {
                if (right[cursor] == -1) {
                    right[cursor] = insert;
                    return;
                }
                cursor = right[cursor];
            } else {
                if (middle[cursor] == -1) {
                    middle[cursor] = insert;
                    return;
                }
                cursor = middle[cursor];
            }
        }
    }

    private static <T extends Comparable<T>> String[] readStringInput(ISort<T> values) {
        String[] out = new String[values.size()];
        for (int i = 0; i < values.size(); i++) {
            T raw = values.get(i);
            if (!(raw instanceof String value)) {
                return new String[0];
            }
            out[i] = value;
        }
        return out;
    }
}


