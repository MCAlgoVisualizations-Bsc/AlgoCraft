package io.github.mcalgovisualizations.algorithms;

import io.github.mcalgovisualizations.visualization.algorithms.IPlayerSort;
import io.github.mcalgovisualizations.visualization.algorithms.events.Compare;
import io.github.mcalgovisualizations.visualization.algorithms.events.Message;
import io.github.mcalgovisualizations.visualization.models.ISort;

import java.util.Arrays;

public final class PlayerBSTBuild implements IPlayerSort {

    @Override
    public <T extends Comparable<T>> void sort(ISort<T> values) {
        int size = values.size();
        if (size == 0) {
            values.emit(new Message("BST build: empty input", Message.MessageType.ERROR));
            return;
        }

        int[] data = readIntInput(values);
        if (data.length == 0) {
            values.emit(new Message("BST build: expected integer values", Message.MessageType.ERROR));
            return;
        }

        int[] left = new int[size];
        int[] right = new int[size];
        Arrays.fill(left, -1);
        Arrays.fill(right, -1);

        int root = 0;
        values.emit(new Message("BST root = " + data[root], Message.MessageType.INFO));

        for (int insert = 1; insert < size; insert++) {
            int candidate = data[insert];
            int cursor = root;

            while (true) {
                int current = data[cursor];
                values.emit(new Compare(cursor, insert, current, candidate));

                if (candidate < current) {
                    if (left[cursor] == -1) {
                        left[cursor] = insert;
                        values.emit(new Message(
                                "Insert " + candidate + " left of " + current,
                                Message.MessageType.HINT
                        ));
                        break;
                    }
                    cursor = left[cursor];
                } else {
                    if (right[cursor] == -1) {
                        right[cursor] = insert;
                        values.emit(new Message(
                                "Insert " + candidate + " right of " + current,
                                Message.MessageType.HINT
                        ));
                        break;
                    }
                    cursor = right[cursor];
                }
            }
        }

        values.emit(new Message("BST build complete", Message.MessageType.SUCCESS));
    }

    @Override
    public String getName() {
        return "BST Build";
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

