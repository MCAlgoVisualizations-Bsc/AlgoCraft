package io.github.mcalgovisualizations.algorithms;

import io.github.mcalgovisualizations.algorithms.context.SortingContext;
import io.github.mcalgovisualizations.visualization.algorithm.IPlayerSort;
import io.github.mcalgovisualizations.events.Compare;

import java.util.Arrays;
import java.util.Objects;

public final class PlayerBSTSearch<I extends Comparable<I>> implements IPlayerSort<SortingContext<I>> {

    @Override
    public void run(SortingContext<I> ctx) {
        var values = ctx.values;
        int size = values.size();
        if (size == 0) {
            return;
        }

        int[] left = new int[size];
        int[] right = new int[size];
        Arrays.fill(left, -1);
        Arrays.fill(right, -1);

        int root = 0;
        for (int insert = 1; insert < size; insert++) {
            var candidate = values.get(insert);
            int cursor = root;
            while (true) {
                if (candidate.compareTo(values.get(cursor)) < 0) {
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
        var target = values.get(targetIndex);

        int cursor = root;
        while (cursor != -1) {
            var current = values.get(cursor);
            ctx.emit(new Compare(cursor, targetIndex, current, target));

            if (Objects.equals(current, target)) {
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
}

