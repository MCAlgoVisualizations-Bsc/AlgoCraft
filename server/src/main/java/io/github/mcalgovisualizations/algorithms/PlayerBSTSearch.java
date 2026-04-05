package io.github.mcalgovisualizations.algorithms;

import io.github.mcalgovisualizations.visualization.algorithms.IPlayerSort;
import io.github.mcalgovisualizations.visualization.algorithms.events.Compare;
import io.github.mcalgovisualizations.visualization.algorithms.events.Message;
import io.github.mcalgovisualizations.visualization.models.ISort;

import java.util.Arrays;

public final class PlayerBSTSearch implements IPlayerSort {

    @Override
    public <T extends Comparable<T>> void sort(ISort<T> values) {
        int size = values.size();
        if (size == 0) {
            values.emit(new Message("BST search: empty input", Message.MessageType.ERROR));
            return;
        }

        int[] data = readIntInput(values);
        if (data.length == 0) {
            values.emit(new Message("BST search: expected integer values", Message.MessageType.ERROR));
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
        values.emit(new Message("Searching for " + target, Message.MessageType.INFO));

        int cursor = root;
        while (cursor != -1) {
            int current = data[cursor];
            values.emit(new Compare(cursor, targetIndex, current, target));

            if (current == target) {
                values.emit(new Message("Found " + target, Message.MessageType.SUCCESS));
                return;
            }

            if (target < current) {
                values.emit(new Message("Go left from " + current, Message.MessageType.HINT));
                cursor = left[cursor];
            } else {
                values.emit(new Message("Go right from " + current, Message.MessageType.HINT));
                cursor = right[cursor];
            }
        }

        values.emit(new Message("Target not found", Message.MessageType.ERROR));
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

