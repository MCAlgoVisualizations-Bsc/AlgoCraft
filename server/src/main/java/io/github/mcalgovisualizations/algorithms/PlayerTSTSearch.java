package io.github.mcalgovisualizations.algorithms;

import io.github.mcalgovisualizations.algorithms.context.GridContext;
import io.github.mcalgovisualizations.algorithms.context.SortingContext;
import io.github.mcalgovisualizations.visualization.algorithm.IPlayerSort;
import io.github.mcalgovisualizations.events.Compare;

import java.util.Arrays;
import java.util.List;

public final class PlayerTSTSearch<T extends Comparable<T>> implements IPlayerSort<GridContext<T>> {

    @Override
    public void run(GridContext<T> ctx) {
        var values = ctx.getData();
        int size = values.size();
        if (size == 0) {
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
            insert(root, insert, values, left, middle, right);
        }

        int targetIndex = size / 2;
        T target = values.get(targetIndex);

        int cursor = root;
        while (cursor != -1) {
            T current = values.get(cursor);
            ctx.emit(new Compare(cursor, targetIndex, current, target));

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

    private void insert(int root, int insert, List<T> data, int[] left, int[] middle, int[] right) {
        int cursor = root;
        T candidate = data.get(insert);

        while (true) {
            T current = data.get(cursor);
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
}


